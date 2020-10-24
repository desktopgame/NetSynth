/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sound.midi.Receiver;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jp.desktopgame.netsynth.NetSynth;
import jp.desktopgame.netsynth.core.action.AutoRecAction;
import jp.desktopgame.netsynth.core.action.AutoRecTask;
import jp.desktopgame.netsynth.midi.MidiDeviceController;
import jp.desktopgame.netsynth.midi.MidiDeviceManager;
import jp.desktopgame.netsynth.mixer.DataLineConnection;
import jp.desktopgame.pec.PropertyEditorDialog;

/**
 *
 * @author desktopgame
 */
public class AutoRecDialog extends JPanel {

    private Action action;
    private JComboBox<String> targetLiens;
    private JComboBox<String> receivers;
    private JSpinner velocitySpinner;
    private JSpinner secondsSpinner;
    private JTextField outputDirTextField;
    private JButton start = new JButton("開始");
    private List<DataLineConnection> connections;
    private List<MidiDeviceController> controllers;

    public AutoRecDialog(Action action) {
        this.action = action;
        this.connections = DataLineConnection.getConnections().stream().filter((e) -> e.isTargetDataLine()).collect(Collectors.toList());
        this.controllers = MidiDeviceManager.getInstance().getDeviceControllers().stream().filter((e) -> e.getReceiver().isPresent()).collect(Collectors.toList());
        this.targetLiens = new JComboBox<String>(connections.stream().map((e) -> e.getUniqueName()).toArray(String[]::new));
        this.receivers = new JComboBox<String>(controllers.stream().map((e) -> e.getAlias()).toArray(String[]::new));
        this.velocitySpinner = new JSpinner(new SpinnerNumberModel(100, 0, 999, 1));
        this.secondsSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1));
        this.outputDirTextField = new JTextField("AutoRec");
        start.addActionListener(this::onStart);
    }

    private void onStart(ActionEvent e) {
        int tl = targetLiens.getSelectedIndex();
        int midi = receivers.getSelectedIndex();
        if (tl < 0 || midi < 0) {
            return;
        }
        DataLineConnection dlc = connections.get(tl);
        Optional<Receiver> receiverOpt = controllers.get(midi).getReceiver();
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
        File dir = new File(outputDirTextField.getText());
        if (!dir.exists()) {
            dir.mkdir();
        }
        int velocity = (int) velocitySpinner.getValue();
        setEnabled(false);
        start.setEnabled(false);
        new AutoRecTask(dir, dlc, receiver, velocity, (int) secondsSpinner.getValue(), start, action).execute();
    }

    public void showDialog() {
        PropertyEditorDialog dialog = new PropertyEditorDialog() {
            {
                setHiddenApplyButton(true);
                setHiddenCancelButton(true);
                setHiddenOKButton(true);
            }

            @Override
            protected void init() {
                setTitle("自動録音の設定");
                super.init(); //To change body of generated methods, choose Tools | Templates.
                addLine("ターゲットライン", targetLiens);
                addLine("MIDIレシーバー", receivers);
                addLine("ベロシティ", velocitySpinner);
                addLine("秒数", secondsSpinner);
                addLine("出力フォルダ", outputDirTextField);
                addLine(start);
                addFooter();
            }
        };
        dialog.show(null);
    }

}
