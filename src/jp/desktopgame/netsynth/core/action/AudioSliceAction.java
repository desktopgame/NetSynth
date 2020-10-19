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
import jp.desktopgame.netsynth.core.SoundSliceEditDialog;

/**
 *
 * @author desktopgame
 */
public class AudioSliceAction extends ViewAction {

    public AudioSliceAction(View view) {
        super(view);
        putValue(NAME, "音源のスライス設定");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        new SoundSliceEditDialog().showDialog();
    }

}
