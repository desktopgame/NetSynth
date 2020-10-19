/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.mixer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

/**
 * オーディオデバイスの一覧を管理するクラスです.
 *
 * @author desktopgame
 */
public class MixerManager {

    private static MixerManager instance;
    private List<MixerController> devices;

    private MixerManager() {
        this.devices = new ArrayList<>();
    }

    /**
     * 唯一のインスタンスを返します.
     *
     * @return
     */
    public static MixerManager getInstance() {
        if (instance == null) {
            instance = new MixerManager();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    DataLineConnection.closeConnections();
                }
            });
        }
        return instance;
    }

    /**
     * 現在の接続状態でオーディオデバイスを再取得します.
     */
    public void fetch() {
        DataLineConnection.closeConnections();
        Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
        for (Mixer.Info minfo : mixerInfo) {
            Mixer mixer = AudioSystem.getMixer(minfo);
            Line.Info[] sources = mixer.getSourceLineInfo();
            Line.Info[] targets = mixer.getTargetLineInfo();
            if (sources.length == 0 && targets.length == 0) {
                continue;
            }
            MixerController device = new MixerController(minfo, mixer);
            try {
                device.access();
                devices.add(device);
            } catch (LineUnavailableException ex) {
                Logger.getLogger(MixerManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public List<MixerController> getDevices() {
        return new ArrayList<>(devices);
    }
}
