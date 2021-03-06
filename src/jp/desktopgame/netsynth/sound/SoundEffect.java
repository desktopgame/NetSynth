/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.sound;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 再生可能なエフェクトです.
 *
 * @author desktopgame
 */
public class SoundEffect {

    private File file;
    private KeyInfo keyInfo;
    private boolean loaded;

    private byte[] audio;
    private AudioFormat format;
    private Clip loopClip;
    private static ExecutorService executor;

    static {
        executor = Executors.newFixedThreadPool(10);
    }

    public SoundEffect(File file) {
        this.file = file;
    }

    private Clip getClip() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        load();
        DataLine.Info info = new DataLine.Info(Clip.class, format);
        Clip clip = (Clip) AudioSystem.getLine(info);
        return clip;
    }

    public void playOneShot() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        Clip clip = getClip();
        clip.open(format, audio, 0, audio.length);
        clip.start();
        clip.addLineListener((event) -> {
            // ストップか最後まで再生された場合
            if (event.getType() == LineEvent.Type.STOP) {
                //Clip clip = (Clip) event.getSource();
                clip.stop();
                clip.setFramePosition(0); // 再生位置を最初に戻す
            }
        });
    }

    public void playLoop() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        load();
        this.loopClip = getClip();
        loopClip.setLoopPoints(0, -1);
        loopClip.loop(Clip.LOOP_CONTINUOUSLY);
        loopClip.open(format, audio, 0, audio.length);
        loopClip.start();
    }

    public void playLoop(long lenPlayMicroSeconds, float peakStart, float peakEnd) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        load();
        this.loopClip = getClip();
        loopClip.open(format, audio, 0, audio.length);
        long len = loopClip.getMicrosecondLength();
        System.out.printf("%d %d¥n", len, lenPlayMicroSeconds);
        // ピーク開始時の時間
        long peakStartMS = (long) ((float) len * peakStart);
        // ピーク終了時の時間
        long peakEndMS = (long) ((float) len * peakEnd);
        // ピーク後の再生停止までの時間
        long peakAfterLenMS = len - peakEndMS;
        // ピーク中の時間
        long peakPlayLenMS = peakEndMS - peakStartMS;
        if (lenPlayMicroSeconds >= len) {
            int frames = loopClip.getFrameLength();
            long loopLenMS = lenPlayMicroSeconds - peakStartMS - peakAfterLenMS;
            int loopCount = (int) (loopLenMS / peakPlayLenMS);
            loopClip.setLoopPoints((int) (frames * peakStart), (int) (frames * peakEnd) - 1);
            loopClip.loop(loopCount);
        }
        loopClip.start();
        loopClip.addLineListener((event) -> {
            // ストップか最後まで再生された場合
            if (event.getType() == LineEvent.Type.STOP) {
                //Clip clip = (Clip) event.getSource();
                loopClip.stop();
                loopClip.setFramePosition(0); // 再生位置を最初に戻す
            }
        });
        // 再生時間が短すぎる場合には所定の再生時間を超えた時点で停止
        // 再生時間が長すぎる場合にはピークを繰り返す->ピーク後から最後まで流す
        if (lenPlayMicroSeconds < len) {
            executor.submit(() -> {
                while (true) {
                    try {
                        TimeUnit.MICROSECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        break;
                    }
                    long posMS = loopClip.getMicrosecondPosition();
                    if (lenPlayMicroSeconds < len && posMS >= lenPlayMicroSeconds) {
                        loopClip.stop();
                        break;
                    }
                }
            });
        }
    }

    public void stop() {
        if (loaded) {
            loopClip.stop();
            loopClip.flush();
            loopClip.setFramePosition(0);
        }
    }

    public void load() throws IOException, UnsupportedAudioFileException {
        if (loaded) {
            return;
        }
        AudioInputStream ais = AudioSystem.getAudioInputStream(file);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        byte[] bytes = new byte[255];
        while (ais.available() > 0) {
            int read = ais.read(bytes, 0, 255);
            byteOut.write(bytes, 0, read);
        }
        this.audio = byteOut.toByteArray();
        this.format = ais.getFormat();
        ais.close();
    }

    public void unload() {
        if (!loaded) {
            return;
        }
        this.audio = null;
        this.format = null;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return file.getName();
    }

    public KeyInfo getKeyInfo() {
        if (keyInfo == null) {
            String name = delSuffix(getName());
            Parser parser = new Parser(name);
            parser.parse();
            this.keyInfo = new KeyInfo(parser.getKey(), parser.getIndex(), parser.getCategory());
        }
        return keyInfo;
    }

    private static boolean isKey(Character c) {
        return Character.isAlphabetic(c) || c == '#';
    }

    private String delSuffix(String filename) {
        int pos = filename.lastIndexOf(".");
        if (pos < 0) {
            return filename;
        }
        return filename.substring(0, pos);
    }

    private String parseSection(int pos, String name, Predicate<Character> pred, boolean continueIfEmpty) {
        Stack<Character> cstack = new Stack<>();
        for (int i = pos; i >= 0; i--) {
            char c = name.charAt(i);
            if (pred.test(c)) {
                cstack.push(c);
            } else {
                break;
            }
        }
        if (continueIfEmpty && cstack.empty()) {
            if (pos == 0) {
                return "";
            }
            return parseSection(pos - 1, name, pred, continueIfEmpty);
        }
        List<Character> clist = new ArrayList<>();
        while (!cstack.empty()) {
            clist.add(cstack.pop());
        }
        StringBuilder sb = new StringBuilder();
        for (Character c : clist) {
            sb.append(c);
        }
        return sb.toString();
    }
}
