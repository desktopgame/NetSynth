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
import javax.swing.Action;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.MixerControlDialog;

/**
 *
 * @author desktopgame
 */
public class MixerControlPanelAction extends ViewAction {

    public MixerControlPanelAction(View view) {
        super(view);
        putValue(Action.NAME, "ミキサーコントロールパネル");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        MixerControlDialog dialog = new MixerControlDialog();
        dialog.showDialog();
    }

}
