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
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.resources.Resources;
import jp.desktopgame.prc.PianoRollEditorPane;

/**
 *
 * @author desktopgame
 */
public class StopAction extends EditorAction {

    public StopAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "停止");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "Stop16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "Stop24")));
        setEnabled(false);
    }

    @Override
    protected boolean canExec(int newEditorIndex) {
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e, PianoRollEditorPane editor) {
        view.getWorkAreaPane().stopSequence();
        editor.getPianoRollLayerUI().setSequencePosition(0);
        view.getAction("PlayAction").setEnabled(true);
        view.getAction("PauseAction").setEnabled(false);
        setEnabled(false);
    }

}
