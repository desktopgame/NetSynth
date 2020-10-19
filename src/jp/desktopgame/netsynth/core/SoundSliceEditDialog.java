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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.tree.DefaultMutableTreeNode;
import jp.desktopgame.asec.AudioSliceEditor;
import static jp.desktopgame.netsynth.NetSynth.logException;
import jp.desktopgame.netsynth.sound.SoundDatabase;
import jp.desktopgame.netsynth.sound.SoundEffect;

/**
 *
 * @author desktopgame
 */
public class SoundSliceEditDialog extends SoundDatabaseDialog {

    private DefaultComboBoxModel<SoundEffect> effectComboBoxModel;
    private JComboBox<SoundEffect> effectComboBox;
    private AudioSliceEditor editor;
    private JSpinner secondsSpinner;
    private JButton playButton, stopButton;

    public SoundSliceEditDialog() {
        super();
        setTitle("サウンドのスライス");
    }

    @Override
    protected void onSelectNode(DefaultMutableTreeNode node) {
        onSelectSoundDatabase((SoundDatabase) node.getUserObject());
    }

    @Override
    protected void onSelectSoundDatabase(SoundDatabase sdb) {
        effectComboBoxModel.removeAllElements();
        for (SoundEffect se : sdb.getEffects()) {
            effectComboBoxModel.addElement(se);
        }
    }

    @Override
    protected void onUnselectSoundDatabase() {
        effectComboBoxModel.removeAllElements();
    }

    @Override
    protected Component createRightComponent() {
        this.effectComboBoxModel = new DefaultComboBoxModel<>();
        this.effectComboBox = new JComboBox<>(effectComboBoxModel);
        effectComboBox.addItemListener(this::onSelectEffect);
        effectComboBox.setRenderer(new SoundEffectListCellRenderer());
        this.editor = new AudioSliceEditor();
        editor.addPropertyChangeListener(this::onPropChange);
        this.secondsSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 100.0, 0.01));
        this.playButton = new JButton("再生");
        this.stopButton = new JButton("停止");
        playButton.addActionListener(this::onPlay);
        stopButton.addActionListener(this::onStop);
        JPanel root = new JPanel(new BorderLayout());
        Box hBox = Box.createHorizontalBox();
        hBox.add(effectComboBox);
        hBox.add(secondsSpinner);
        hBox.add(new JLabel("秒"));
        hBox.add(playButton);
        hBox.add(stopButton);
        root.add(hBox, BorderLayout.NORTH);
        root.add(editor, BorderLayout.CENTER);
        return root;
    }

    private void onPlay(ActionEvent e) {
        SoundEffect eff = (SoundEffect) effectComboBox.getSelectedItem();
        if (eff == null) {
            return;
        }
        double sec = (double) secondsSpinner.getValue();
        SoundSampleSetting setting = GlobalSetting.Context.getGlobalSetting().getSampleSetting(eff.getFile().getPath());
        sec *= 1000000;
        try {
            eff.playLoop((int) sec, setting.peakStart, setting.peakEnd);
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
            logException(ex);
        }
    }

    private void onStop(ActionEvent e) {
        SoundEffect eff = (SoundEffect) effectComboBox.getSelectedItem();
        if (eff == null) {
            return;
        }
        eff.stop();
    }

    private boolean ignore;

    private void onSelectEffect(ItemEvent e) {
        SoundEffect eff = (SoundEffect) effectComboBox.getSelectedItem();
        if (eff == null) {
            return;
        }
        ignore = true;
        SoundSampleSetting setting = GlobalSetting.Context.getGlobalSetting().getSampleSetting(eff.getFile().getPath());
        editor.setPeakStartPosition(setting.peakStart);
        editor.setPeakEndPosition(setting.peakEnd);
        ignore = false;
    }

    private void onPropChange(PropertyChangeEvent e) {
        if (ignore) {
            return;
        }
        String name = e.getPropertyName();
        if (name.equals("peakStartPosition") || name.equals("peakEndPosition")) {
            SoundEffect eff = (SoundEffect) effectComboBox.getSelectedItem();
            if (eff == null) {
                return;
            }
            SoundSampleSetting setting = GlobalSetting.Context.getGlobalSetting().getSampleSetting(eff.getFile().getPath());
            setting.peakStart = editor.getPeakStartPosition();
            setting.peakEnd = editor.getPeakEndPosition();
            try {
                GlobalSetting.Context.getInstance().save();
            } catch (IOException ex) {
                logException(ex);
            }
        }
    }
}
