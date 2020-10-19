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
import jp.desktopgame.netsynth.core.MidiControlDialog;

/**
 *
 * @author desktopgame
 */
public class MidiControlPanelAction extends ViewAction {

    public MidiControlPanelAction(View view) {
        super(view);
        putValue(Action.NAME, "MIDIコントロールパネル");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        MidiControlDialog dialog = new MidiControlDialog();
        dialog.showDialog();
    }

}
