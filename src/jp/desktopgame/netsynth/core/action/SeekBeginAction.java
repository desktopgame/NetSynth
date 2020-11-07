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

/**
 *
 * @author desktopgame
 */
public class SeekBeginAction extends EditorAction {

    public SeekBeginAction(View view) {
        super(view);
        setEnabled(false);
        putValue(AbstractAction.NAME, "先頭へ");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "StepBack16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "StepBack24")));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        view.getWorkAreaPane().getAllTrackEditor().stream().map((e) -> e.getPianoRollLayerUI()).forEach((e) -> {
            e.seekToBegin(true);
        });
    }

}
