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
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.resources.Resources;
import jp.desktopgame.prc.PianoRollEditorPane;

/**
 *
 * @author desktopgame
 */
public class CutAction extends EditorAction {

    public CutAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "切り取り");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Cut16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Cut24")));
        putValue(AbstractAction.MNEMONIC_KEY, (int) 'X');
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e, PianoRollEditorPane editor) {
        editor.cut();
    }

}
