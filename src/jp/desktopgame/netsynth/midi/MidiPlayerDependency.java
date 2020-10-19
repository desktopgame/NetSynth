/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

/**
 * {@link jp.desktopgame.netsynth.midi.MidiPlayerSetting}に加えてMidiイベントを生成するための付加情報が含まれたクラスです.
 *
 * @author desktopgame
 * @param <T>
 */
public class MidiPlayerDependency<T> {

    public final String synthesizer;
    public final T userObject;
    public final MidiPlayerSetting setting;

    public MidiPlayerDependency(String synthesizer, T userObject, MidiPlayerSetting setting) {
        this.synthesizer = synthesizer;
        this.userObject = userObject;
        this.setting = setting;
    }

}
