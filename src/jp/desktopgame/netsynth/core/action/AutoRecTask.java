/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.action;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.SwingWorker;
import jp.desktopgame.netsynth.NetSynth;
import jp.desktopgame.netsynth.mixer.DataLineConnection;
import jp.desktopgame.prc.Keyboard;

/**
 *
 * @author desktopgame
 */
public class AutoRecTask extends SwingWorker<Void, Void> {

    private File dir;
    private DataLineConnection dataLineConnection;
    private Receiver receiver;
    private int velocity;
    private int seconds;
    private JButton button;
    private Action action;

    public AutoRecTask(File dir, DataLineConnection dataLineConnection, Receiver receiver, int velocity, int seconds, JButton button, Action action) {
        this.dir = dir;
        this.dataLineConnection = dataLineConnection;
        this.receiver = receiver;
        this.velocity = velocity;
        this.seconds = seconds;
        this.button = button;
        this.action = action;
    }

    @Override
    protected Void doInBackground() throws Exception {

        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 12; j++) {
                String chord = Keyboard.KEY_STRING_TABLE[j];
                int key = ((i * 12) + j);
                if (key >= 128) {
                    continue;
                }
                dataLineConnection.flush();
                try {
                    dataLineConnection.open(dataLineConnection.getLineAudioFormat());
                } catch (LineUnavailableException ex) {
                    NetSynth.logException(ex);
                }
                try {
                    dataLineConnection.start();
                } catch (LineUnavailableException ex) {
                    NetSynth.logException(ex);
                }
                try {
                    receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, key, velocity), -1);
                } catch (InvalidMidiDataException ex) {
                    NetSynth.logException(ex);
                }
                try {
                    Thread.sleep(1000 * seconds);
                } catch (InterruptedException ex) {
                    NetSynth.logException(ex);
                }
                try {
                    receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, key, velocity), -1);
                } catch (InvalidMidiDataException ex) {
                    NetSynth.logException(ex);
                }
                try {
                    dataLineConnection.stop();
                } catch (InterruptedException ex) {
                    NetSynth.logException(ex);
                }
                try {
                    dataLineConnection.write(new File(dir, chord + "_" + i + ".wav"));
                } catch (IOException ex) {
                    NetSynth.logException(ex);
                }
            }
        }
        dataLineConnection.close();
        return null;
    }

    @Override
    protected void done() {
        super.done(); //To change body of generated methods, choose Tools | Templates.
        button.setEnabled(true);
        action.setEnabled(true);
    }

}
