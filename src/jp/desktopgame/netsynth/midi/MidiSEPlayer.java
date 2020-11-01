/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import jp.desktopgame.netsynth.core.GlobalSetting;
import jp.desktopgame.netsynth.core.SoundSampleSetting;
import jp.desktopgame.netsynth.sound.SoundDatabase;
import jp.desktopgame.netsynth.sound.SoundEffect;

/**
 *
 * @author desktopgame
 */
public class MidiSEPlayer extends MidiDirectPlayer {

    private SoundDatabase sdb;
    private boolean isMute;

    public MidiSEPlayer(SoundDatabase sdb) {
        this.sdb = sdb;
    }

    private void playSE(int h, boolean on) {
        GlobalSetting gs = GlobalSetting.Context.getGlobalSetting();
        Optional<SoundEffect> seOpt = sdb.getEffects().stream().filter((e) -> gs.getSampleSetting(e.getFile().getPath()).keyHeight == h).findFirst();
        Optional<SoundSampleSetting> settingOpt = sdb.getEffects().stream().map((e) -> gs.getSampleSetting(e.getFile().getPath())).filter((e) -> e.keyHeight == h).findFirst();
        seOpt.ifPresent((e) -> {
            if (on) {
                try {
                    e.playLoop(settingOpt.get().peakStart, settingOpt.get().peakEnd);
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                    Logger.getLogger(MidiSEPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                e.stop();
            }
        });
    }

    @Override
    public void virtualPlay(VirtualMidiEvent e) {
        if (isMute) {
            return;
        }
        playSE(e.height, e.noteOn);
    }

    @Override
    protected void send(MidiEvent e) {
        if (isMute) {
            return;
        }
        MidiMessage msg = e.getMessage();
        if (msg instanceof ShortMessage) {
            ShortMessage smsg = (ShortMessage) msg;
            playSE(smsg.getData1(), smsg.getCommand() == ShortMessage.NOTE_ON);
        }
    }

    @Override
    public void programChange(int bank, int program) {
    }

    @Override
    public void mute(boolean isMute) {
        this.isMute = isMute;
    }

    @Override
    public void allNotesOff() {
    }

    @Override
    public void allSoundOff() {

    }
}
