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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import jp.desktopgame.netsynth.sound.SoundEffect;

/**
 *
 * @author desktopgame
 */
public class SoundEffectListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
        if (!(value instanceof SoundEffect)) {
            return this;
        }
        SoundEffect se = (SoundEffect) value;
        setText(se.getName());
        return this;
    }

}
