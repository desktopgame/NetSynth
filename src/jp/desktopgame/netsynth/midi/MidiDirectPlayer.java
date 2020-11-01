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
import java.util.Optional;
import javax.sound.midi.MidiEvent;

/**
 *
 * @author desktopgame
 */
public abstract class MidiDirectPlayer implements MidiPlayer, VirtualMidiListener {

    private MidiPlayerStatus status;
    private List<MidiEvent> events;
    private VirtualMidiSequencer vseq;
    private Optional<MidiPlayerSetting> settingOpt;
    private boolean pause;
    private boolean exit;
    private int timebase, bpm;

    public MidiDirectPlayer() {
        this.settingOpt = Optional.empty();
        this.status = MidiPlayerStatus.WAITING;
    }

    @Override
    public final void setup(MidiPlayerSetting setting, List<MidiEvent> events, int timebase, int bpm) {
        this.settingOpt = Optional.of(setting);
        this.events = new ArrayList<>(events);
        this.timebase = timebase;
        this.bpm = bpm;
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
