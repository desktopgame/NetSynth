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

    private MidiPlayerStatus status;
    private VirtualMidiSequencer vseq;
    private int timebase, bpm;

    public MidiDirectPlayer() {
        this.status = MidiPlayerStatus.WAITING;
    }

    @Override
    public final void setup(MidiPlayerSetting setting) {
        this.vseq = setting.virtualMidiSequencer;
        vseq.addVirtualMidiListener(this);
        this.status = MidiPlayerStatus.SYNC_PLAYING;
        programChange(setting.bank, setting.program);
        mute(setting.isMute);
    }

    @Override
    public void reset() {
        if (this.status == MidiPlayerStatus.SYNC_PLAYING) {
            vseq.removeVirtualMidiListener(this);
        }
    }

    @Override
    public MidiPlayerStatus getStatus() {
        return status;
    }

    @Override
    public abstract void virtualPlay(VirtualMidiEvent e);

    protected abstract void send(MidiEvent e);
}
