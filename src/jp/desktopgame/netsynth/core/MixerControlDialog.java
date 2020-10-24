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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Mixer;
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
import jp.desktopgame.netsynth.mixer.DataLineConnection;
import jp.desktopgame.netsynth.mixer.MixerController;
import jp.desktopgame.netsynth.mixer.MixerManager;
import jp.desktopgame.pec.PropertyEditorPane;

/**
 *
 * @author desktopgame
 */
public class MixerControlDialog extends JPanel {

    private List<MixerController> devices;
    private JComboBox<String> deviceComboBox;
    private DefaultComboBoxModel<String> deviceComboBoxModel;
    private PropertyEditorPane propEditorPane;
    private JButton refreshButton;
    private JTabbedPane tabbedPane;

    private JTextField propNameTextField;
    private JTextField propDescTextField;
    private JTextField propVendorTextField;
    private JTextField propVersionTextField;

    private DefaultTableModel sourceLineTableModel;
    private JTable sourceLineTable;

    private DefaultTableModel targetLineTableModel;
    private JTable targetLineTable;

    public MixerControlDialog() {
        this.deviceComboBoxModel = new DefaultComboBoxModel<>();
        this.deviceComboBox = new JComboBox<>(deviceComboBoxModel);
        this.refreshButton = new JButton("更新");
        this.tabbedPane = new JTabbedPane();
        this.propNameTextField = readonlyTextField();
        this.propDescTextField = readonlyTextField();
        this.propVendorTextField = readonlyTextField();
        this.propVersionTextField = readonlyTextField();

        this.sourceLineTableModel = new DefaultTableModel(new String[]{"名前", "状態", "サンプルレート", "各サンプルのビット数", "チャンネル数", "符号", "ビッグエンディアン"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.sourceLineTable = new JTable(sourceLineTableModel);
        this.targetLineTableModel = new DefaultTableModel(new String[]{"名前", "状態", "サンプルレート", "各サンプルのビット数", "チャンネル数", "符号", "ビッグエンディアン"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.targetLineTable = new JTable(targetLineTableModel);
        deviceComboBox.addItemListener(this::onSelect);
        refreshButton.addActionListener(this::onRefresh);
        this.propEditorPane = new PropertyEditorPane() {
            {
                addLine("名前", propNameTextField);
                addLine("説明", propDescTextField);
                addLine("ベンダー", propVendorTextField);
                addLine("バージョン", propVersionTextField);
                addFooter(tabbedPane);
            }
        };
        JPanel sourceLinePanel = new JPanel(new BorderLayout());
        JPanel targetLinePanel = new JPanel(new BorderLayout());
        tabbedPane.addTab("ソースライン", sourceLinePanel);
        tabbedPane.addTab("ターゲットライン", targetLinePanel);
        sourceLinePanel.add(new JScrollPane(sourceLineTable), BorderLayout.CENTER);
        targetLinePanel.add(new JScrollPane(targetLineTable), BorderLayout.CENTER);
        JPanel bar = new JPanel(new BorderLayout());
        bar.add(deviceComboBox, BorderLayout.CENTER);
        bar.add(refreshButton, BorderLayout.EAST);
        reload();
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

    private void onRefresh(ActionEvent e) {
        reload();
    }

    private void onSelect(ItemEvent e) {
        int i = deviceComboBox.getSelectedIndex();
        if (i < 0) {
            return;
        }
        MixerController dev = devices.get(i);
        Mixer.Info minfo = dev.getMixerInfo();
        propNameTextField.setText(dev.getMixerName());
        propDescTextField.setText(minfo.getDescription());
        propVendorTextField.setText(minfo.getVendor());
        propVersionTextField.setText(minfo.getVersion());
        while (sourceLineTableModel.getRowCount() > 0) {
            sourceLineTableModel.removeRow(0);
        }
        while (targetLineTableModel.getRowCount() > 0) {
            targetLineTableModel.removeRow(0);
        }
        for (String a : dev.getSourceLineAliases()) {
            DataLineConnection line = dev.getSourceLines().get(dev.getSourceLineIndexFromAlias(a));
            AudioFormat fmt = line.getLineAudioFormat();
            sourceLineTableModel.addRow(new Object[]{a, line.isOpen() ? "Opened" : "Closed", fmt.getSampleRate(), fmt.getSampleSizeInBits(), fmt.getChannels(), fmt.getEncoding(), fmt.isBigEndian()});
        }
        for (String a : dev.getTargetAliases()) {
            DataLineConnection line = dev.getTargetLines().get(dev.getTargetLineIndexFromAlias(a));
            AudioFormat fmt = line.getLineAudioFormat();
            targetLineTableModel.addRow(new Object[]{a, line.isOpen() ? "Opened" : "Closed", fmt.getSampleRate(), fmt.getSampleSizeInBits(), fmt.getChannels(), fmt.getEncoding(), fmt.isBigEndian()});
        }
    }

    private void reload() {
        this.devices = new ArrayList<>(MixerManager.getInstance().getControllers());
        deviceComboBoxModel.removeAllElements();
        Map<String, Integer> data = new HashMap<>();
        for (String name : devices.stream().map((e) -> e.getMixerName()).collect(Collectors.toList())) {
            if (data.containsKey(name)) {
                name = name + "." + data.get(name);
            }
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
        JDialog dialog = new JDialog();
        dialog.setTitle("ミキサーコントロールパネル");
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.add(this);
        dialog.pack();
        dialog.setModal(true);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
