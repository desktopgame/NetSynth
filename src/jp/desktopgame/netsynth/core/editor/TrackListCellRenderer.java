/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.editor;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import jp.desktopgame.prc.PianoRollGroup;

/**
 *
 * @author desktopgame
 */
public class TrackListCellRenderer extends DefaultListCellRenderer {

    private PianoRollGroup group;

    public TrackListCellRenderer(PianoRollGroup group) {
        this.group = group;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
        label.setForeground(group.getSkinColor(index));
        return label;
    }

}
