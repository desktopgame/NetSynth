/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

import javax.sound.midi.MidiEvent;

/**
 *
 * @author desktopgame
 */
public abstract class MidiDirectPlayer implements MidiPlayer, VirtualMidiListener {

    private VirtualMidiSequencer vseq;

    public MidiDirectPlayer() {
    }

    @Override
    public final void setup(MidiPlayerSetting setting) {
        this.vseq = setting.virtualMidiSequencer;
        vseq.addVirtualMidiListener(this);
        programChange(setting.bank, setting.program);
        mute(setting.isMute);
    }

    @Override
    public void reset() {
        vseq.removeVirtualMidiListener(this);
    }

    @Override
    public abstract void virtualPlay(VirtualMidiEvent e);

    protected abstract void send(MidiEvent e);
}
