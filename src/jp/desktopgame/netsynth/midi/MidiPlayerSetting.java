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
 * Midiプレーヤーの設定情報です.
 *
 * @author desktopgame
 */
public class MidiPlayerSetting {

    /**
     * この設定情報を使用するMidiプレーヤーがGUIと同期して音を再生する場合にのみ使用されます.
     */
    public final VirtualMidiSequencer virtualMidiSequencer;
    /**
     * ミュートするなら true.
     */
    public final boolean isMute;
    /**
     * 使用するバンク番号.
     */
    public final int bank;
    /**
     * 使用するプログラム番号.
     */
    public final int program;
    /**
     * GUIと同期するなら true .
     */
    public final boolean sync;

    public MidiPlayerSetting(VirtualMidiSequencer virtualMidiSequencer, boolean isMute, int bank, int program, boolean sync) {
        this.virtualMidiSequencer = virtualMidiSequencer;
        this.isMute = isMute;
        this.bank = bank;
        this.program = program;
        this.sync = sync;
    }

}
