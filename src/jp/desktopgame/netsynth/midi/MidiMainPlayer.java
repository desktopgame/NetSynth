/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

import java.util.ArrayList;
import java.util.List;

/**
 * MIDIの依存関係設定から
 *
 * @author desktopgame
 * @param <T>
 */
public class MidiMainPlayer<T> {

    private MidiPlayerStatus status;
    private List<MidiPlayerDependency<T>> dependencies;
    private List<MidiPlayer> players;
    private MidiEventFactory<T> eventFactory;
    private int timebase;
    private int bpm;
    private int beatWidth;

    public MidiMainPlayer(MidiEventFactory<T> eventFactory, int timebase, int bpm, int beatWidth) {
        this.eventFactory = eventFactory;
        this.status = MidiPlayerStatus.WAITING;
        this.players = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.timebase = timebase;
        this.bpm = bpm;
        this.beatWidth = beatWidth;
    }

    public void addDependency(MidiPlayerDependency<T> setting) {
        dependencies.add(setting);
    }

    public MidiPlayerDependency<T> getDependency(int i) {
        return dependencies.get(i);
    }

    public void removeDependency(MidiPlayerDependency<T> setting) {
        dependencies.remove(setting);
    }

    public void removeDependency(int i) {
        dependencies.remove(i);
    }

    public void clearDependency() {
        players.forEach(MidiPlayer::reset);
        players.clear();
        dependencies.clear();
    }

    /**
     * 現在の依存関係でMIDIプレイヤーを構築します.
     */
    public void setup() {
        MidiResolver<T> resolver = new MidiResolver<>(eventFactory, dependencies, timebase, bpm, beatWidth);
        this.players = new ArrayList<>(resolver.resolve());
    }

    public void setTimebase(int timebase) {
        this.timebase = timebase;
    }

    public int getTimebase() {
        return timebase;
    }

    public void setBPM(int bpm) {
        this.bpm = bpm;
    }

    public int getBPM() {
        return bpm;
    }

    public void setBeatWidth(int beatWidth) {
        this.beatWidth = beatWidth;
    }

    public int getBeatWidth() {
        return beatWidth;
    }

}
