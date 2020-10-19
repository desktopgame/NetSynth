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
import jp.desktopgame.netsynth.mixer.DataLineConnection;

/**
 *
 * @author desktopgame
 */
public class AllLineCloseAction extends ViewAction {

    public AllLineCloseAction(View view) {
        super(view);
        putValue(NAME, "全て閉じる");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DataLineConnection.closeConnections();
    }

}
