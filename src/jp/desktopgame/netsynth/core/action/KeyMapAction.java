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
import jp.desktopgame.netsynth.core.SoundMapDialog;

/**
 *
 * @author desktopgame
 */
public class KeyMapAction extends ViewAction {

    public KeyMapAction(View view) {
        super(view);
        putValue(NAME, "音源とキーのマッピング");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        new SoundMapDialog().showDialog();
    }

}
