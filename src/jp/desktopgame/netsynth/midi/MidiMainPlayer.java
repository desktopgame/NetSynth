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
import javax.swing.SwingWorker;

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
    private final Object COMMAND_LOCK = new Object();
    private final Object SETTING_LOCK = new Object();

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
        synchronized (SETTING_LOCK) {
            dependencies.add(setting);
        }
    }

    public void removeDependency(MidiPlayerDependency<T> setting) {
        synchronized (SETTING_LOCK) {
            dependencies.remove(setting);
        }
    }

    public void removeDependency(int i) {
        synchronized (SETTING_LOCK) {
            dependencies.remove(i);
        }
    }

    public void clearDependency() {
        synchronized (SETTING_LOCK) {
            players.forEach(MidiPlayer::reset);
            players.clear();
            dependencies.clear();
        }
    }

    /**
     * 現在の依存関係でMIDIプレイヤーを構築します.
     */
    public void setup() {
        synchronized (SETTING_LOCK) {
            MidiResolver<T> resolver = new MidiResolver<>(eventFactory, dependencies, timebase, bpm, beatWidth);
            this.players = new ArrayList<>(resolver.resolve());
        }
    }

    public void start() {
        synchronized (SETTING_LOCK) {
            if (this.status == MidiPlayerStatus.WAITING) {
                playImpl();
                this.status = MidiPlayerStatus.PLAYING;
            }
        }
    }

    public void pause() {
        synchronized (COMMAND_LOCK) {
            if (this.status == MidiPlayerStatus.SYNC_PLAYING) {
                return;
            }
            players.forEach(MidiPlayer::pause);
        }
        if (players.stream().allMatch((e) -> e.getStatus() == MidiPlayerStatus.PAUSED)) {
            this.status = MidiPlayerStatus.PAUSED;
        }
    }

    public void resume() {
        synchronized (COMMAND_LOCK) {
            if (this.status == MidiPlayerStatus.SYNC_PLAYING) {
                return;
            }
            players.forEach(MidiPlayer::resume);
        }
        if (players.stream().allMatch((e) -> e.getStatus() == MidiPlayerStatus.PLAYING)) {
            this.status = MidiPlayerStatus.PLAYING;
        }
    }

    public void stop() {
        synchronized (COMMAND_LOCK) {
            if (this.status == MidiPlayerStatus.SYNC_PLAYING) {
                return;
            }
            players.forEach(MidiPlayer::stop);
            execPoll();
        }
    }

    private void execPoll() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (true) {
                    synchronized (COMMAND_LOCK) {
                        if (players.stream().allMatch((e) -> e.getStatus() == MidiPlayerStatus.WAITING)) {
                            break;
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                super.done(); //To change body of generated methods, choose Tools | Templates.
                status = MidiPlayerStatus.WAITING;
            }

        }.execute();
    }

    private void playImpl() {
        synchronized (COMMAND_LOCK) {
            players.forEach(MidiPlayer::start);
        }
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
