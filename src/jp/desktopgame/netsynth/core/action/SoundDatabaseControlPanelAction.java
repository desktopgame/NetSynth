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
import jp.desktopgame.netsynth.core.SoundDatabaseControlDialog;

/**
 *
 * @author desktopgame
 */
public class SoundDatabaseControlPanelAction extends ViewAction {

    public SoundDatabaseControlPanelAction(View view) {
        super(view);
        putValue(Action.NAME, "サウンドデータベースコントロールパネル");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        new SoundDatabaseControlDialog().showDialog();

    }

}
