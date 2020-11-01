/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author desktopgame
 */
public class MidiReceiverPlayer extends MidiDirectPlayer {

    private Receiver receiver;
    private boolean isMute;

    public MidiReceiverPlayer(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void programChange(int bank, int program) {
    }

    @Override
    public void mute(boolean isMute) {
        this.isMute = isMute;
    }

    @Override
    protected void send(MidiEvent e) {
        if (!isMute) {
            receiver.send(e.getMessage(), -1);
        }
    }

    @Override
    public void virtualPlay(VirtualMidiEvent e) {
        if (!isMute) {
            try {
                receiver.send(new ShortMessage(e.noteOn ? ShortMessage.NOTE_ON : ShortMessage.NOTE_OFF, e.height, e.velocity), 0);
            } catch (InvalidMidiDataException ex) {
                Logger.getLogger(MidiReceiverPlayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void allNotesOff() {
    }

    @Override
    public void allSoundOff() {
    }
}
