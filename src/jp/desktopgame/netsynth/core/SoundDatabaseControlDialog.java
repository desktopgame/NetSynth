/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import java.awt.Component;
import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import static jp.desktopgame.netsynth.NetSynth.logException;
import jp.desktopgame.netsynth.sound.SoundDatabase;
import jp.desktopgame.netsynth.sound.SoundEffect;

/**
 *
 * @author desktopgame
 */
public class SoundDatabaseControlDialog extends SoundDatabaseDialog {

    private DefaultListModel<SoundEffect> effectListModel;
    private JList<SoundEffect> effectList;

    public SoundDatabaseControlDialog() {
        super();
        setTitle("サウンドデータベースコントロールパネル");
    }

    private void onSelectItem(ListSelectionEvent e) {
        try {
            SoundEffect se = effectList.getSelectedValue();
            if (se == null) {
                return;
            }
            se.playOneShot();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
            logException(ex);
        }
    }

    @Override
    protected Component createRightComponent() {
        this.effectListModel = new DefaultListModel<>();
        this.effectList = new JList<>(effectListModel);
        effectList.setCellRenderer(new SoundEffectListCellRenderer());
        effectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        effectList.addListSelectionListener(this::onSelectItem);
        return new JScrollPane(effectList);
    }

    @Override
    protected void onSelectNode(DefaultMutableTreeNode selectedNode) {
        SoundDatabase sdb = (SoundDatabase) selectedNode.getUserObject();
        effectListModel.removeAllElements();
        sdb.getEffects().forEach(effectListModel::addElement);
    }

    @Override
    protected void onUnselectSoundDatabase() {
        effectListModel.clear();
    }

}
