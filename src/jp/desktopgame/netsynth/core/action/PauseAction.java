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
public class PauseAction extends EditorAction {

    public PauseAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "一時停止");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "Pause16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "Pause24")));
        setEnabled(false);
    }

    @Override
    protected boolean canExec(int newEditorIndex) {
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e, PianoRollEditorPane editor) {
        view.getWorkAreaPane().pauseSequence();
        view.getAction("PlayAction").setEnabled(true);
        view.getAction("StopAction").setEnabled(true);
        setEnabled(false);
    }

}
