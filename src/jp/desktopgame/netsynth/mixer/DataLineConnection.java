/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.mixer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * オーディオの入力ライン/出力ラインを管理するクラスです.
 *
 * @author desktopgame
 */
public class DataLineConnection {

    private static List<DataLineConnection> lineConnections;
    private Mixer mixer;
    private DataLine dataLine;
    private String alias;
    private boolean open;
    private boolean mark;
    private AudioFormat audioFormat;

    private byte[] audioData;
    private boolean flushBuffer;

    private Thread thread;
    private boolean transport;
    private boolean terminate;
    private final Object MUTEX = new Object();

    static {
        lineConnections = new ArrayList<>();
    }

    private DataLineConnection(Mixer mixer, DataLine dataLine, String alias) {
        this.mixer = mixer;
        this.dataLine = dataLine;
        this.open = dataLine.isOpen();
        this.mark = open;
        this.alias = alias;
        lineConnections.add(this);
    }

    /**
     * 指定のデータラインからコネクションを作成します.
     *
     * @param dataLine
     * @return
     */
    public static DataLineConnection newConnection(Mixer mixer, DataLine dataLine, String alias) {
        return lineConnections.stream().filter((e) -> e.dataLine.equals(dataLine)).findFirst().map((e) -> e).orElse(new DataLineConnection(mixer, dataLine, alias));
    }

    /**
     * 全てのコネクションを閉じます.
     */
    public static void closeConnections() {
        lineConnections.stream().forEach(DataLineConnection::close);
        lineConnections.clear();
    }

    /**
     * 全てのコネクションを返します.
     *
     * @return
     */
    public static List<DataLineConnection> getConnections() {
        return new ArrayList<>(lineConnections);
    }

    /**
     * 指定のフォーマットでデータラインを開きます.
     * ただし、SourceDataLineでもTargetDataLineでもなかった場合には引数は無視されます。 既に開いている場合には何もしません。
     *
     * @param format
     * @throws LineUnavailableException
     */
    public void open(AudioFormat format) throws LineUnavailableException {
        if (open) {
            return;
        }
        if (dataLine instanceof SourceDataLine) {
            ((SourceDataLine) dataLine).open(format);
        } else if (dataLine instanceof TargetDataLine) {
            ((TargetDataLine) dataLine).open(format);
        } else {
            dataLine.open();
        }
        dataLine.start();
        this.open = true;
        this.mark = true;
        this.audioFormat = format;
    }

    /**
     * コネクションを閉じます.既に閉じている場合には何もしません。
     */
    public void close() {
        if (!open) {
            return;
        }
        dataLine.stop();
        dataLine.close();
        this.open = false;
        this.mark = false;
    }

    /**
     * データラインをオープンしたときのAudioFormatに対応するソースデータラインに転送を開始します.
     *
     * @throws LineUnavailableException
     */
    public void start() throws LineUnavailableException {
        start(AudioSystem.getSourceDataLine(audioFormat));
    }

    /**
     * 指定のソースデータラインへの転送を開始します.
     *
     * @param sdl
     */
    public void start(SourceDataLine sdl) {
        if (!open) {
            throw new IllegalStateException();
        }
        synchronized (MUTEX) {
            if (this.transport) {
                return;
            }
            this.transport = true;
        }
        this.thread = new Thread(() -> doRun(sdl));
        thread.start();
    }

    /**
     * 転送を停止します. コネクションは閉じられます。
     *
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        synchronized (MUTEX) {
            if (!this.transport) {
                return;
            }
            this.terminate = true;
        }
        close();
        thread.join();
        thread = null;
    }

    /**
     * 録音データを破棄します.
     */
    public void flush() {
        if (!open) {
            return;
        }
        synchronized (MUTEX) {
            this.flushBuffer = true;
        }
    }

    private void doRun(SourceDataLine sdl) {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        try {
            AudioInputStream ais = newInputStream().get();
            //sdl = AudioSystem.getSourceDataLine(audioFormat);

            sdl.open(audioFormat);
            sdl.start();
            byte[] temp = new byte[256];
            while (true) {
                int writeBytes = 0;
                while (true) {
                    if (ais.available() <= 0) {
                        break;
                    }
                    int read = ais.read(temp);
                    if (read <= 0) {
                        break;
                    }
                    writeBytes++;
                    sdl.write(temp, 0, read);
                    byteOut.write(temp, 0, read);
                    if (read < temp.length - 1) {
                        break;
                    }
                }
                if (writeBytes > 0) {
                    sdl.drain();
                }
                synchronized (MUTEX) {
                    if (flushBuffer) {
                        this.flushBuffer = false;
                        byteOut.reset();
                    }
                    if (terminate) {
                        break;
                    }
                }
            }
        } catch (LineUnavailableException | IOException ex) {
            Logger.getLogger(DataLineConnection.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (sdl != null) {
                sdl.stop();
                sdl.close();
            }
            synchronized (MUTEX) {
                this.terminate = false;
                this.transport = false;
                this.audioData = byteOut.toByteArray();
            }
        }
    }

    /**
     * データラインがTargetDataLineであるなら新しいAudioInputStreamを作成して返します.
     *
     * @return
     */
    public Optional<AudioInputStream> newInputStream() {
        if (isTargetDataLine()) {
            return Optional.of(new AudioInputStream((TargetDataLine) dataLine));
        }
        return Optional.empty();
    }

    /**
     * 最後に録音したデータをファイルへ書き込みます.
     *
     * @param file
     * @throws IOException
     */
    public void write(File file) throws IOException {
        write(audioFormat, file);
    }

    /**
     * 最後に録音したデータをファイルへ書き込みます.
     *
     * @param format
     * @param file
     * @throws IOException
     */
    public void write(AudioFormat format, File file) throws IOException {
        synchronized (MUTEX) {
            if (audioData == null || audioData.length == 0) {
                return;
            }
            long length = audioData.length / format.getFrameSize();
            ByteArrayInputStream byteIn = new ByteArrayInputStream(getAudioData());
            AudioInputStream audioIn = new AudioInputStream(byteIn, format, length);
            AudioSystem.write(audioIn, AudioFileFormat.Type.WAVE, file);
        }
    }

    /**
     * マーク状態を現在のオープン状態で上書きします.
     */
    public void resetMark() {
        setMark(open);
    }

    /**
     * マーク状態を設定します.
     *
     * @param mark
     */
    public void setMark(boolean mark) {
        this.mark = mark;
    }

    /**
     * 開いているなら true を返します.
     *
     * @return
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * マーク状態を返します.
     *
     * @return
     */
    public boolean isMark() {
        return mark;
    }

    /**
     * データラインがSourceDataLineであるなら true を返します.
     *
     * @return
     */
    public boolean isSourceDataLine() {
        return dataLine instanceof SourceDataLine;
    }

    /**
     * データラインがTargetDataLineであるなら true を返します.
     *
     * @return
     */
    public boolean isTargetDataLine() {
        return dataLine instanceof TargetDataLine;
    }

    /**
     * データラインのフォーマットを返します.
     *
     * @return
     */
    public AudioFormat getLineAudioFormat() {
        return dataLine.getFormat();
    }

    /**
     * データラインの詳細情報を返します.
     *
     * @return
     */
    public Line.Info getLineInfo() {
        return dataLine.getLineInfo();
    }

    /**
     * データラインから最後に録音したデータを返します.
     *
     * @return
     */
    public byte[] getAudioData() {
        return audioData;
    }

    /**
     * ミキサーを返します.
     *
     * @return
     */
    public Mixer getMixer() {
        return mixer;
    }

    /**
     * 別名を返します.
     *
     * @return
     */
    public String getAlias() {
        return alias;
    }

    public String getUniqueName() {
        return getMixer().getMixerInfo().getName() + "#" + getAlias();
    }
}
