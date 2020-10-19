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
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jp.desktopgame.netsynth.core.action.LineConnectAction;
import jp.desktopgame.netsynth.mixer.DataLineConnection;
import jp.desktopgame.netsynth.mixer.MixerController;
import jp.desktopgame.netsynth.mixer.MixerManager;
import jp.desktopgame.pec.PropertyEditorDialog;

/**
 *
 * @author desktopgame
 */
public class LineConnectDialog extends JPanel {

    private DefaultComboBoxModel<String> mixerComboBoxModel;
    private JComboBox<String> mixerComboBox;

    private DefaultComboBoxModel<String> lineComboBoxModel;
    private JComboBox<String> lineComboBox;

    private JButton openButton;
    private JButton closeButton;

    private MixerManager mixerMan;
    private List<DataLineConnection> openedLineList;
    private List<DataLineConnection> closedLineList;

    public LineConnectDialog() {
        super(new BorderLayout());
        this.mixerComboBoxModel = new DefaultComboBoxModel<>();
        this.mixerComboBox = new JComboBox<>(mixerComboBoxModel);
        this.lineComboBoxModel = new DefaultComboBoxModel<>();
        this.lineComboBox = new JComboBox<>(lineComboBoxModel);
        this.openButton = new JButton("Open");
        this.closeButton = new JButton("Close");
        this.mixerMan = MixerManager.getInstance();
        this.openedLineList = new ArrayList<>();
        this.closedLineList = new ArrayList<>();
        // 全てのマーク状態をリセット
        mixerMan.getDevices().stream().flatMap((e) -> e.getTargetLines().stream()).forEach((e) -> e.resetMark());

        openButton.setEnabled(false);
        closeButton.setEnabled(false);
        openButton.addActionListener(this::onOpen);
        closeButton.addActionListener(this::onClose);
        mixerComboBox.addItemListener(this::onMixerSelect);
        lineComboBox.addItemListener(this::onLineSelect);
        mixerMan.getDevices().forEach((mixerCon) -> {
            mixerComboBoxModel.addElement(mixerCon.getMixerName());
        });
    }

    public List<DataLineConnection> getOpenedLines() {
        return new ArrayList<>(openedLineList);
    }

    public List<DataLineConnection> getClosedLines() {
        return new ArrayList<>(closedLineList);
    }

    private void onOpen(ActionEvent e) {
        int i = lineComboBox.getSelectedIndex();
        if (i >= 0) {
            MixerController mixerCon = mixerMan.getDevices().get(mixerComboBox.getSelectedIndex());
            DataLineConnection line = mixerCon.getTargetLines().get(mixerCon.getTargetLineIndexFromAlias(lineComboBoxModel.getElementAt(i)));
            //AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
            //line.open(format);
            line.setMark(true);
            closedLineList.remove(line);
            openedLineList.add(line);
            openButton.setEnabled(false);
            closeButton.setEnabled(true);
        }
    }

    private void onClose(ActionEvent e) {
        int i = lineComboBox.getSelectedIndex();
        if (i >= 0) {
            MixerController mixerCon = mixerMan.getDevices().get(mixerComboBox.getSelectedIndex());
            DataLineConnection line = mixerCon.getTargetLines().get(mixerCon.getTargetLineIndexFromAlias(lineComboBoxModel.getElementAt(i)));
            line.setMark(false);
            openedLineList.remove(line);
            closedLineList.add(line);
            openButton.setEnabled(true);
            closeButton.setEnabled(false);
        }
    }

    private void onMixerSelect(ItemEvent e) {
        int i = mixerComboBox.getSelectedIndex();
        if (i >= 0) {
            MixerController mixerCon = mixerMan.getDevices().get(i);
            while (lineComboBoxModel.getSize() > 0) {
                lineComboBoxModel.removeElementAt(0);
            }
            mixerCon.getTargetAliases().stream().forEach((line) -> {
                lineComboBoxModel.addElement(line);
            });
            SwingUtilities.invokeLater(() -> {
                int ii = lineComboBox.getSelectedIndex();
                if (ii >= 0) {
                    updateButtonState(mixerCon, ii);
                }
            });
        }
    }

    private void onLineSelect(ItemEvent e) {
        int i = lineComboBox.getSelectedIndex();
        if (i >= 0) {
            MixerController mixerCon = mixerMan.getDevices().get(mixerComboBox.getSelectedIndex());
            updateButtonState(mixerCon, i);
        }
    }

    private void updateButtonState(MixerController mixerCon, int i) {
        String key = lineComboBoxModel.getElementAt(i);
        int index = mixerCon.getTargetLineIndexFromAlias(key);
        if (index < 0) {
            openButton.setEnabled(false);
            closeButton.setEnabled(false);
            return;
        }
        DataLineConnection line = mixerCon.getTargetLines().get(index);
        openButton.setEnabled(!line.isMark());
        closeButton.setEnabled(line.isMark());
    }

    public void showDialog() {

        PropertyEditorDialog dialog = new PropertyEditorDialog() {
            {
                setHiddenApplyButton(true);
                setHiddenCancelButton(true);
                setTitle("ライン接続");
            }

            @Override
            protected void init() {
                super.init(); //To change body of generated methods, choose Tools | Templates.
                mixerComboBox.setSelectedIndex(0);
                addLine("ミキサー", mixerComboBox);
                addLine("ターゲットライン", lineComboBox);
                addLine(openButton);
                addLine(closeButton);
                addFooter();
            }
        };
        dialog.show(null);
    }

}
