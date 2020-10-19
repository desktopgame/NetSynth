/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jp.desktopgame.netsynth.NetSynth;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.midi.MidiDeviceController;
import jp.desktopgame.netsynth.midi.MidiDeviceManager;
import jp.desktopgame.netsynth.mixer.DataLineConnection;
import jp.desktopgame.pec.PropertyEditorDialog;
import jp.desktopgame.prc.Keyboard;

/**
 *
 * @author desktopgame
 */
public class AutoRecAction extends ViewAction {

    public AutoRecAction(View view) {
        super(view);
        putValue(NAME, "自動録音");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        List<DataLineConnection> conns = DataLineConnection.getConnections();
        List<MidiDeviceController> conts = MidiDeviceManager.getInstance().getDeviceControllers().stream().filter((e) -> e.getReceiver().isPresent()).collect(Collectors.toList());
        PropertyEditorDialog dialog = new PropertyEditorDialog() {
            private JComboBox<String> targetLiens;
            private JComboBox<String> receivers;
            private JSpinner velocitySpinner;

            {
                setHiddenApplyButton(true);
                setHiddenCancelButton(true);
                setHiddenOKButton(true);
            }

            @Override
            protected void init() {
                setTitle("自動録音の設定");
                JButton start = new JButton("開始");

                start.addActionListener(this::onStart);
                super.init(); //To change body of generated methods, choose Tools | Templates.
                addLine("ターゲットライン", targetLiens = new JComboBox<String>(conns.stream().map((e) -> e.getUniqueName()).toArray(String[]::new)));
                addLine("MIDIレシーバー", receivers = new JComboBox<String>(conts.stream().map((e) -> e.getAlias()).toArray(String[]::new)));
                addLine("ベロシティ", velocitySpinner = new JSpinner(new SpinnerNumberModel(100, 0, 999, 1)));
                addLine(start);
                addFooter();
            }

            private void onStart(ActionEvent e) {
                int tl = targetLiens.getSelectedIndex();
                int midi = receivers.getSelectedIndex();
                if (tl < 0 || midi < 0) {
                    return;
                }
                DataLineConnection dlc = conns.get(tl);
                Optional<Receiver> receiverOpt = conts.get(midi).getReceiver();
                if (!receiverOpt.isPresent()) {
                    NetSynth.logInformation("指定された名前のレシーバが存在しません。");
                    return;
                }
                dlc.close();
                try {
                    dlc.stop();
                } catch (InterruptedException ex) {
                    NetSynth.logException(ex);
                }
                Receiver receiver = receiverOpt.get();
                File dir = new File(dlc.getUniqueName());
                if (!dir.exists()) {
                    dir.mkdir();
                }
                int vel = (int) velocitySpinner.getValue();

                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 12; j++) {
                        String chord = Keyboard.KEY_STRING_TABLE[j];
                        int key = (i * 12) + j;
                        dlc.flush();
                        try {
                            dlc.open(dlc.getLineAudioFormat());
                        } catch (LineUnavailableException ex) {
                            NetSynth.logException(ex);
                        }
                        try {
                            dlc.start();
                        } catch (LineUnavailableException ex) {
                            NetSynth.logException(ex);
                        }
                        try {
                            receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 0, key, vel), -1);
                        } catch (InvalidMidiDataException ex) {
                            NetSynth.logException(ex);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            NetSynth.logException(ex);
                        }
                        try {
                            receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 0, key, vel), -1);
                        } catch (InvalidMidiDataException ex) {
                            NetSynth.logException(ex);
                        }
                        try {
                            dlc.stop();
                        } catch (InterruptedException ex) {
                            NetSynth.logException(ex);
                        }
                        try {
                            dlc.write(new File(dir, chord + "_" + i + ".wav"));
                        } catch (IOException ex) {
                            NetSynth.logException(ex);
                        }
                    }
                }
                dlc.close();
            }
        };
        dialog.show(null);
    }

}
