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
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import jp.desktopgame.netsynth.sound.SoundDatabase;

/**
 *
 * @author desktopgame
 */
public class SoundDatabaseTreeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus); //To change body of generated methods, choose Tools | Templates.
        value = ((DefaultMutableTreeNode) value).getUserObject();
        if (!(value instanceof SoundDatabase)) {
            return this;
        }
        SoundDatabase sdb = (SoundDatabase) value;
        this.setText(sdb.getName());
        return this;
    }

}
