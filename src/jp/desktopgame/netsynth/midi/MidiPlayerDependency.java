/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

import java.util.Optional;

/**
 * {@link jp.desktopgame.netsynth.midi.MidiPlayerSetting}に加えてMidiイベントを生成するための付加情報が含まれたクラスです.
 *
 * @author desktopgame
 */
public class MidiPlayerDependency {

    public final String synthesizer;
    public final MidiPlayerSetting setting;
    public Optional<MidiPlayer> player;

    public MidiPlayerDependency(String synthesizer, MidiPlayerSetting setting) {
        this.synthesizer = synthesizer;
        this.setting = setting;
        this.player = Optional.empty();
    }

}
