/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author desktopgame
 */
public class MidiChannelPlayer extends MidiDirectPlayer {

    private MidiChannel channel;
    private boolean isMute;

    public MidiChannelPlayer(MidiChannel channel) {
        this.channel = channel;
    }

    @Override
    public void programChange(int bank, int program) {
        channel.programChange(bank, program);
    }

    @Override
    public void mute(boolean isMute) {
        channel.setMute(isMute);
        this.isMute = isMute;
    }

    @Override
    protected void send(MidiEvent e) {
        if (isMute) {
            return;
        }
        MidiMessage msg = e.getMessage();
        if (msg instanceof ShortMessage) {
            ShortMessage smsg = (ShortMessage) msg;
            if (smsg.getCommand() == ShortMessage.NOTE_ON) {
                channel.noteOn(smsg.getData1(), smsg.getData2());
            } else if (smsg.getCommand() == ShortMessage.NOTE_OFF) {
                channel.noteOff(smsg.getData1(), smsg.getData2());
            }
        }
    }

    @Override
    public void virtualPlay(VirtualMidiEvent e) {
        if (isMute) {
            return;
        }
        if (e.noteOn) {
            channel.noteOn(e.height, e.velocity);
        } else {
            channel.noteOff(e.height, 0);
        }
    }
}
