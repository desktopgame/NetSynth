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
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.AutoRecDialog;

/**
 * 自動録音のためのアクションです.
 *
 * @author desktopgame
 */
public class AutoRecAction extends ViewAction {

    public AutoRecAction(View view) {
        super(view);
        putValue(NAME, "自動録音");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        AutoRecDialog.showDialog(this);
    }

}
