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
 */
public class MidiMainPlayer {

    private MidiPlayerStatus status;
    private List<MidiPlayerDependency> dependencies;
    private List<MidiPlayer> players;

    public MidiMainPlayer() {
        this.status = MidiPlayerStatus.WAITING;
        this.players = new ArrayList<>();
        this.dependencies = new ArrayList<>();
    }

    public void addDependency(MidiPlayerDependency setting) {
        dependencies.add(setting);
    }

    public MidiPlayerDependency getDependency(int i) {
        return dependencies.get(i);
    }

    public void removeDependency(MidiPlayerDependency setting) {
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
        MidiResolver resolver = new MidiResolver(dependencies);
        this.players = new ArrayList<>(resolver.resolve());
    }

}
