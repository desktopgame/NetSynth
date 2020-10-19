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
public class PasteAction extends EditorAction {

    public PasteAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "貼り付け");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Paste16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Paste24")));
        putValue(AbstractAction.MNEMONIC_KEY, (int) 'V');
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e, PianoRollEditorPane editor) {
        editor.paste();
    }

}
