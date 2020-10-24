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
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.midi.Synthesizer;
import static jp.desktopgame.netsynth.NetSynth.logException;

/**
 * Midiデバイスの一覧を管理するクラスです.
 *
 * @author desktopgame
 */
public class MidiDeviceManager {

    private static MidiDeviceManager instance;
    private List<MidiDeviceController> deviceControllers;

    private MidiDeviceManager() {
        this.deviceControllers = new ArrayList<>();
    }

    /**
     * 唯一のインスタンスを返します.
     *
     * @return
     */
    public static MidiDeviceManager getInstance() {
        if (instance == null) {
            instance = new MidiDeviceManager();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    instance.clear();
                }
            });
        }
        return instance;
    }

    /**
     * 全てのデバイスを閉じます.
     */
    public void clear() {
        for (MidiDeviceController controller : deviceControllers) {
            controller.unlockHandle();
        }
        this.deviceControllers.clear();
    }

    /**
     * 現在の接続状態でMIDIデバイスを再取得します.
     */
    public void fetch() {
        clear();
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();

        for (MidiDevice.Info info : infos) {
            try {
                MidiDevice device = MidiSystem.getMidiDevice(info);
                String alias = info.getName();
                String basename = new String(alias.toCharArray());
                int c = 1;
                while (isTest(alias)) {
                    alias = basename + "." + (c++);
                }
                MidiDeviceController con = new MidiDeviceController(info, device, alias);
                con.lockHandle();
                this.deviceControllers.add(con);
            } catch (MidiUnavailableException ex) {
                logException(ex);
                this.deviceControllers.add(new MidiDeviceController(info, null, "null_device"));
            }
        }
    }

    private boolean isTest(String alias) {
        return deviceControllers.stream().anyMatch((e) -> e.getAlias().equals(alias));
    }

    /**
     * 全てのデバイスを返します.
     *
     * @return
     */
    public List<MidiDeviceController> getDeviceControllers() {
        return new ArrayList<>(deviceControllers);
    }
}
