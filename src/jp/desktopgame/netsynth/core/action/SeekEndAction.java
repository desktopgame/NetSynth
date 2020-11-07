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
public class SeekEndAction extends EditorAction {

    public SeekEndAction(View view) {
        super(view);
        setEnabled(false);
        putValue(AbstractAction.NAME, "末尾へ");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "StepForward16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "StepForward24")));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        view.getWorkAreaPane().getAllTrackEditor().stream().map((e) -> e.getPianoRollLayerUI()).forEach((e) -> {
            e.seekToEnd(true);
        });
    }

}
