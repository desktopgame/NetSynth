/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.midi.Synthesizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import jp.desktopgame.netsynth.NetSynth;
import jp.desktopgame.netsynth.midi.MidiDeviceController;
import jp.desktopgame.netsynth.midi.MidiDeviceManager;
import jp.desktopgame.pec.PropertyEditorPane;

/**
 *
 * @author desktopgame
 */
public class MidiControlDialog extends JPanel {

    private List<MidiDeviceController> controllers;
    private JComboBox<String> deviceComboBox;
    private DefaultComboBoxModel<String> deviceComboBoxModel;
    private JButton refreshButton;
    private PropertyEditorPane propEditorPane;
    private JTabbedPane tabbedPane;
    private JTable soundBankTable;
    private DefaultTableModel soundBankTableModel;
    private JTable otherResourceTable;
    private DefaultTableModel otherResourceTableModel;

    private JTextField propNameTextField;
    private JTextField propDescTextField;
    private JTextField propVendorTextField;
    private JTextField propVersionTextField;
    private JTextField propKindTextField;
    private JTextField propMaxTransmitterTextField;
    private JTextField propMaxReceiverTextField;
    private JTextField propMaxChannelTextField;

    public MidiControlDialog() {
        this.deviceComboBoxModel = new DefaultComboBoxModel<>();
        this.deviceComboBox = new JComboBox<>(deviceComboBoxModel);
        this.refreshButton = new JButton("更新");
        this.propNameTextField = readonlyTextField();
        this.propDescTextField = readonlyTextField();
        this.propVendorTextField = readonlyTextField();
        this.propVersionTextField = readonlyTextField();
        this.propKindTextField = readonlyTextField();
        this.propMaxTransmitterTextField = readonlyTextField();
        this.propMaxReceiverTextField = readonlyTextField();
        this.propMaxChannelTextField = readonlyTextField();
        this.tabbedPane = new JTabbedPane();
        this.propEditorPane = new PropertyEditorPane() {
            {
                addLine("名前", propNameTextField);
                addLine("説明", propDescTextField);
                addLine("ベンダー", propVendorTextField);
                addLine("バージョン", propVersionTextField);
                addLine("種別", propKindTextField);
                addLine("最大トランスミッタ数", propMaxTransmitterTextField);
                addLine("最大レシーバ数", propMaxReceiverTextField);
                addLine("最大チャンネル数", propMaxChannelTextField);
                addFooter(tabbedPane);
            }
        };
        this.soundBankTableModel = new DefaultTableModel(new String[]{"バンク", "プログラム", "名称"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.soundBankTable = new JTable(soundBankTableModel);
        this.otherResourceTableModel = new DefaultTableModel(new String[]{"名前", "型", "値"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.otherResourceTable = new JTable(otherResourceTableModel);
        JPanel soundBankPanel = new JPanel(new BorderLayout());
        JPanel otherResourcePanel = new JPanel(new BorderLayout());
        tabbedPane.addTab("サウンドバンク", soundBankPanel);
        tabbedPane.addTab("その他のリソース", otherResourcePanel);
        soundBankPanel.add(new JScrollPane(soundBankTable), BorderLayout.CENTER);
        otherResourcePanel.add(new JScrollPane(otherResourceTable), BorderLayout.CENTER);
        refreshButton.addActionListener(this::onRefresh);
        deviceComboBox.addItemListener(this::onSelect);
        reload();
        // レイアウト処理
        JPanel bar = new JPanel(new BorderLayout());
        bar.add(deviceComboBox, BorderLayout.CENTER);
        bar.add(refreshButton, BorderLayout.EAST);
        setLayout(new BorderLayout());
        add(bar, BorderLayout.NORTH);
        add(propEditorPane, BorderLayout.CENTER);
        setPreferredSize(new Dimension(800, 600));
    }

    private JTextField readonlyTextField() {
        JTextField tf = new JTextField();
        tf.setEditable(false);
        return tf;
    }

    private void onSelect(ItemEvent e) {
        int i = deviceComboBox.getSelectedIndex();
        if (i < 0) {
            return;
        }
        MidiDeviceController con = controllers.get(i);
        MidiDevice.Info dev = con.getInfo();
        propNameTextField.setText(dev.getName());
        propDescTextField.setText(dev.getDescription());
        propVendorTextField.setText(dev.getVendor());
        propVersionTextField.setText(dev.getVersion());
        int maxT = con.getMaxTransmitters();
        int maxR = con.getMaxReceivers();
        propMaxTransmitterTextField.setText(maxT == -1 ? "無制限" : String.valueOf(maxT));
        propMaxReceiverTextField.setText(maxR == -1 ? "無制限" : String.valueOf(maxR));
        propMaxChannelTextField.setText("0");

        if (con.isSequencer() && con.isSynthesizer()) {
            propKindTextField.setText("シーケンサー/シンセサイザー");
            propMaxChannelTextField.setText(String.valueOf(con.getSynthesizer().get().getChannels().length));
        } else if (con.isSequencer()) {
            propKindTextField.setText("シーケンサー");
        } else if (con.isSynthesizer()) {
            propKindTextField.setText("シンセサイザー");
            propMaxChannelTextField.setText(String.valueOf(con.getSynthesizer().get().getChannels().length));
        } else {
            propKindTextField.setText("不明");
        }
        while (soundBankTableModel.getRowCount() > 0) {
            soundBankTableModel.removeRow(0);
        }
        while (otherResourceTableModel.getRowCount() > 0) {
            otherResourceTableModel.removeRow(0);
        }
        if (con.isSynthesizer()) {
            Synthesizer synthesizer = con.getSynthesizer().get();
            Soundbank bank = synthesizer.getDefaultSoundbank();
            for (Instrument inst : synthesizer.getAvailableInstruments()) {
                Patch p = inst.getPatch();
                soundBankTableModel.addRow(new Object[]{p.getBank(), p.getProgram(), inst.toString()});
            }
            for (SoundbankResource res : bank.getResources()) {
                otherResourceTableModel.addRow(new Object[]{res.getName(), res.getDataClass(), res.getData()});
            }
        }
    }

    private void onRefresh(ActionEvent e) {
        reload();
    }

    private void reload() {
        this.controllers = new ArrayList<>(MidiDeviceManager.getInstance().getDeviceControllers());
        deviceComboBoxModel.removeAllElements();
        Map<String, Integer> data = new HashMap<>();
        for (String name : controllers.stream().map((e) -> e.getAlias()).collect(Collectors.toList())) {
            deviceComboBoxModel.addElement(name);
            if (!data.containsKey(name)) {
                data.put(name, 1);
            } else {
                data.put(name, data.get(name) + 1);
            }
        }
        deviceComboBox.setSelectedIndex(0);
    }

    public void showDialog() {
        JDialog dialog = new JDialog(NetSynth.getView().getFrame());
        dialog.setTitle("MIDIコントロールパネル");
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.add(this);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);
        dialog.setVisible(true);
    }

}
