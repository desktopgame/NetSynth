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
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import jp.desktopgame.netsynth.View;

/**
 *
 * @author desktopgame
 */
public class VersionAction extends ViewAction {

    public VersionAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "バージョン情報");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        JOptionPane.showMessageDialog(null, "NetSynth - v0.1");
    }

}
