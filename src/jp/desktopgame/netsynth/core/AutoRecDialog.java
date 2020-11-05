/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sound.midi.Receiver;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import jp.desktopgame.netsynth.NetSynth;
import jp.desktopgame.netsynth.core.action.AutoRecTask;
import jp.desktopgame.netsynth.midi.MidiDeviceController;
import jp.desktopgame.netsynth.midi.MidiDeviceManager;
import jp.desktopgame.netsynth.mixer.DataLineConnection;
import jp.desktopgame.pec.builder.ComboBoxHelper;
import jp.desktopgame.pec.builder.IntegerSpinnerHelper;
import jp.desktopgame.pec.builder.PropertyEditorBuilder;
import jp.desktopgame.pec.builder.TextFieldHelper;

/**
 * 自動録音を行うためのダイアログです.
 *
 * @author desktopgame
 */
public class AutoRecDialog extends JPanel {

    private Action action;
    private List<DataLineConnection> connections;
    private List<MidiDeviceController> controllers;

    public AutoRecDialog(Action action) {
        this.action = action;
        this.connections = DataLineConnection.getConnections().stream().filter((e) -> e.isTargetDataLine()).collect(Collectors.toList());
        this.controllers = MidiDeviceManager.getInstance().getDeviceControllers().stream().filter((e) -> e.getReceiver().isPresent()).collect(Collectors.toList());
    }

    public void showDialog() {
        PropertyEditorBuilder pb = new PropertyEditorBuilder();
        ComboBoxHelper<String> targetLines = pb.comboBox("ターゲットライン").overwrite((Object[]) connections.stream().map((e) -> e.getUniqueName()).toArray(String[]::new));
        ComboBoxHelper<String> receivers = pb.comboBox("MIDIレシーバー").overwrite((Object[]) controllers.stream().map((e) -> e.getAlias()).toArray(String[]::new));
        IntegerSpinnerHelper velocity = pb.intSpinner("ベロシティ").range(0, 0, 127, 1);
        IntegerSpinnerHelper seconds = pb.intSpinner("秒数").range(1, 0, 100, 1);
        TextFieldHelper outputDir = pb.textField("出力フォルダ").overwrite("AutoRec");
        JButton start = pb.footer(new JButton("開始"));
        start.addActionListener((e) -> {
            int tl = targetLines.index();
            int midi = receivers.index();
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
            File dir = new File(outputDir.text());
            if (!dir.exists()) {
                dir.mkdir();
            }
            setEnabled(false);
            start.setEnabled(false);
            new AutoRecTask(dir, dlc, receiver, velocity.current(), seconds.current(), start, action).execute();
        });
        pb.buildDialog("自動録音").show(null);
    }

}
