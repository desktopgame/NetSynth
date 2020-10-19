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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
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
    private Thread thread;
    private boolean pause;
    private boolean exit;
    private int timebase, bpm;
    private final Object monitor = new Object();

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
        if (setting.sync) {
            vseq.addVirtualMidiListener(this);
            this.status = MidiPlayerStatus.SYNC_PLAYING;
        }
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
    public void start() {
        synchronized (monitor) {
            checkSync();
            if (this.status != MidiPlayerStatus.WAITING) {
                return;
            }
            this.status = MidiPlayerStatus.PLAYING;
            this.pause = false;
            this.exit = false;
        }
        this.thread = new Thread(this::tryDoRun);
        thread.start();
    }

    @Override
    public void pause() {
        synchronized (monitor) {
            checkSync();
            if (this.status != MidiPlayerStatus.PLAYING) {
                return;
            }
            this.status = MidiPlayerStatus.PAUSED;
            this.pause = true;
        }
        thread.interrupt();
    }

    @Override
    public void resume() {
        synchronized (monitor) {
            checkSync();
            if (this.status != MidiPlayerStatus.PAUSED) {
                return;
            }
            this.status = MidiPlayerStatus.PLAYING;
            this.pause = false;
            monitor.notifyAll();
        }
    }

    @Override
    public void stop() {
        synchronized (monitor) {
            checkSync();
            if (this.status == MidiPlayerStatus.PAUSED) {
                this.pause = false;
                this.exit = true;
                monitor.notifyAll();
            } else if (this.status == MidiPlayerStatus.PLAYING) {
                this.exit = true;
            } else {
                return;
            }
        }
    }

    @Override
    public MidiPlayerStatus getStatus() {
        synchronized (monitor) {
            return status;
        }
    }

    private void checkSync() {
        if (this.status == MidiPlayerStatus.SYNC_PLAYING) {
            throw new IllegalStateException();
        }
    }

    @Override
    public abstract void virtualPlay(VirtualMidiEvent e);

    private void tryDoRun() {
        try {
            doRun();
        } catch (InvalidMidiDataException ex) {
            Logger.getLogger(MidiReceiverPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doRun() throws InvalidMidiDataException {
        long time = 0;
        MidiPlayerSetting setting = settingOpt.get();
        for (MidiEvent e : events) {
            float timebase = this.timebase;
            float bpm = this.bpm;
            float secPerTick = 60f / bpm / timebase;
            float ttf = e.getTick() * secPerTick;
            long tt = (long) (ttf * 1000f);
            if (tt > time) {
                long waitTime = tt - time;
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(waitTime / 10);
                    } catch (InterruptedException ex) {
                        waitForResume();
                    }
                    if (Thread.interrupted()) {
                        waitForResume();
                    }
                }
            }
            // ポーズ
            if (Thread.interrupted()) {
                waitForResume();
            }
            synchronized (monitor) {
                if (exit) {
                    exit = false;
                    status = MidiPlayerStatus.WAITING;
                    break;
                }
            }
            time = tt;
            send(e);
        }
        synchronized (monitor) {
            this.exit = false;
            this.status = MidiPlayerStatus.WAITING;
        }
    }

    private void waitForResume() {
        synchronized (monitor) {
            while (pause) {
                try {
                    monitor.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MidiReceiverPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    protected abstract void send(MidiEvent e);
}
