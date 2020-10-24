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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sound.midi.Receiver;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jp.desktopgame.netsynth.NetSynth;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.midi.MidiDeviceController;
import jp.desktopgame.netsynth.midi.MidiDeviceManager;
import jp.desktopgame.netsynth.mixer.DataLineConnection;
import jp.desktopgame.pec.PropertyEditorDialog;

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
        List<DataLineConnection> conns = DataLineConnection.getConnections().stream().filter((e) -> e.isTargetDataLine()).collect(Collectors.toList());
        List<MidiDeviceController> conts = MidiDeviceManager.getInstance().getDeviceControllers().stream().filter((e) -> e.getReceiver().isPresent()).collect(Collectors.toList());
        PropertyEditorDialog dialog = new PropertyEditorDialog() {
            private JComboBox<String> targetLiens;
            private JComboBox<String> receivers;
            private JSpinner velocitySpinner;
            private JSpinner secondsSpinner;
            private JTextField outputDirTextField;
            private JButton start = new JButton("開始");

            {
                setHiddenApplyButton(true);
                setHiddenCancelButton(true);
                setHiddenOKButton(true);
            }

            @Override
            protected void init() {
                setTitle("自動録音の設定");

                start.addActionListener(this::onStart);
                super.init(); //To change body of generated methods, choose Tools | Templates.
                addLine("ターゲットライン", targetLiens = new JComboBox<String>(conns.stream().map((e) -> e.getUniqueName()).toArray(String[]::new)));
                addLine("MIDIレシーバー", receivers = new JComboBox<String>(conts.stream().map((e) -> e.getAlias()).toArray(String[]::new)));
                addLine("ベロシティ", velocitySpinner = new JSpinner(new SpinnerNumberModel(100, 0, 999, 1)));
                addLine("秒数", secondsSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 999, 1)));
                addLine("出力フォルダ", outputDirTextField = new JTextField("AutoRec"));
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
                File dir = new File(outputDirTextField.getText());
                if (!dir.exists()) {
                    dir.mkdir();
                }
                int velocity = (int) velocitySpinner.getValue();
                setEnabled(false);
                start.setEnabled(false);
                new AutoRecTask(dir, dlc, receiver, velocity, (int) secondsSpinner.getValue(), start, AutoRecAction.this).execute();
            }
        };
        dialog.show(null);
    }

}
