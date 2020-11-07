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
public class PlayAction extends EditorAction {

    public PlayAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "開始");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "Play16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "Play24")));
        putValue(AbstractAction.MNEMONIC_KEY, (int) 'P');
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control P"));
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent e, PianoRollEditorPane editor) {
        if (view.getAction("StopAction").isEnabled()) {
            view.getWorkAreaPane().getTrackEditorManager().resumeSequence();
        } else {
            view.getWorkAreaPane().getTrackEditorManager().playSequence();
        }
        view.getAction("PauseAction").setEnabled(true);
        view.getAction("StopAction").setEnabled(true);
        setEnabled(false);
    }

}
