/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.mixer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 *
 * @author desktopgame
 */
public class MixerController {

    private Mixer.Info mixerInfo;
    private Mixer mixer;
    private Line.Info[] sourceLineInfos;
    private Line.Info[] targetLineInfos;

    private List<DataLineConnection> sourceLines;
    private List<DataLineConnection> targetLines;

    private List<String> sourceLineAliases;
    private List<String> targetLineAliases;

    public MixerController(Mixer.Info mixerInfo, Mixer mixer) {
        this.mixerInfo = Objects.requireNonNull(mixerInfo);
        this.mixer = Objects.requireNonNull(mixer);
        this.sourceLineInfos = mixer.getSourceLineInfo();
        this.targetLineInfos = mixer.getTargetLineInfo();
        this.sourceLines = new ArrayList<>();
        this.targetLines = new ArrayList<>();
        this.sourceLineAliases = new ArrayList<>();
        this.targetLineAliases = new ArrayList<>();
    }

    /* package private */ void access() throws LineUnavailableException {
        int i = 0;
        for (Line.Info sourceInfo : sourceLineInfos) {
            Line sl = mixer.getLine(sourceInfo);
            if (sl instanceof SourceDataLine) {
                String a = "Source." + (++i);
                sourceLines.add(DataLineConnection.newConnection(mixer, (DataLine) sl, a));
                sourceLineAliases.add(a);
            }
        }
        i = 0;
        for (Line.Info targetInfo : targetLineInfos) {
            Line tl = mixer.getLine(targetInfo);
            if (tl instanceof TargetDataLine) {
                String a = "Target." + (++i);
                targetLines.add(DataLineConnection.newConnection(mixer, (DataLine) tl, a));
                targetLineAliases.add(a);
            }
        }
    }

    /**
     * ミキサー名を返します. Windowsでは適切にエンコードされた文字列を返します。
     *
     * @return
     */
    public String getMixerName() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            // https://d-kami.hatenablog.com/entry/2017/09/29/211239
            return new String(mixerInfo.getName().getBytes(StandardCharsets.ISO_8859_1), Charset.forName("Shift_JIS"));
        }
        return mixerInfo.getName();
    }

    public Mixer.Info getMixerInfo() {
        return mixerInfo;
    }

    public Mixer getMixer() {
        return mixer;
    }

    public List<Line.Info> getSourceLineInfoList() {
        return Arrays.asList(sourceLineInfos);
    }

    public List<DataLineConnection> getSourceLines() {
        return new ArrayList<>(sourceLines);
    }

    public List<Line.Info> getTargetLineInfoList() {
        return Arrays.asList(targetLineInfos);
    }

    public List<DataLineConnection> getTargetLines() {
        return new ArrayList<>(targetLines);
    }

    public List<String> getSourceLineAliases() {
        return new ArrayList<>(sourceLineAliases);
    }

    public List<String> getTargetAliases() {
        return new ArrayList<>(targetLineAliases);
    }

    public int getSourceLineIndexFromAlias(String alias) {
        return sourceLineAliases.indexOf(alias);
    }

    public int getTargetLineIndexFromAlias(String alias) {
        return targetLineAliases.indexOf(alias);
    }
}
