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
import jp.desktopgame.netsynth.core.LookAndFeelDialog;

/**
 *
 * @author desktopgame
 */
public class LookAndFeelAction extends ViewAction {

    public LookAndFeelAction(View view) {
        super(view);
        putValue(Action.NAME, "ルックアンドフィールの変更");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new LookAndFeelDialog().showDialog();
    }

}
