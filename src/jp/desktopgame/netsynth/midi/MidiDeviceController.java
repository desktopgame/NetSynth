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
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import static jp.desktopgame.netsynth.NetSynth.logException;

/**
 *
 * @author desktopgame
 */
public class MidiDeviceController {

    private MidiDevice.Info info;
    private Optional<MidiDevice> deviceOpt;
    private int rc;
    private String alias;

    public MidiDeviceController(MidiDevice.Info info, MidiDevice device, String alias) {
        this.info = info;
        this.deviceOpt = Optional.ofNullable(device);
        this.alias = alias;
    }

    public MidiDevice.Info getInfo() {
        return info;
    }

    public String getAlias() {
        return alias;
    }

    private void doOpen() {
        deviceOpt.ifPresent((e) -> {
            try {
                this.rc++;
                if (this.rc == 1) {
                    e.open();
                }
            } catch (MidiUnavailableException ex) {
                logException(ex);
            }
        });
    }

    private void doClose() {
        deviceOpt.ifPresent((e) -> {
            this.rc--;
            if (rc == 0) {
                e.close();
            }
        });
    }

    public int getMaxReceivers() {
        if (!deviceOpt.isPresent()) {
            return 0;
        }
        return deviceOpt.get().getMaxReceivers();
    }

    public int getMaxTransmitters() {
        if (!deviceOpt.isPresent()) {
            return 0;
        }
        return deviceOpt.get().getMaxTransmitters();
    }

    public Optional<Sequencer> getSequencer() {
        if (isSequencer()) {
            return Optional.of((Sequencer) deviceOpt.get());
        }
        return Optional.empty();
    }

    public Optional<Synthesizer> getSynthesizer() {
        if (isSynthesizer()) {
            return Optional.of((Synthesizer) deviceOpt.get());
        }
        return Optional.empty();
    }

    public boolean isSequencer() {
        if (deviceOpt.isPresent()) {
            return deviceOpt.get() instanceof Sequencer;
        }
        return false;
    }

    public boolean isSynthesizer() {
        if (deviceOpt.isPresent()) {
            return deviceOpt.get() instanceof Synthesizer;
        }
        return false;
    }

    public Optional<Receiver> getReceiver() {
        return deviceOpt.map((e) -> {
            try {
                return e.getReceiver();
            } catch (MidiUnavailableException ex) {
                //logException(ex);
                return null;
            }
        });
    }

    public Optional<Transmitter> getTransmitter() {
        return deviceOpt.map((e) -> {
            try {
                return e.getTransmitter();
            } catch (MidiUnavailableException ex) {
                logException(ex);
                return null;
            }
        });
    }

    public void lockHandle() {
        doOpen();
    }

    public void unlockHandle() {
        doClose();
    }
}
