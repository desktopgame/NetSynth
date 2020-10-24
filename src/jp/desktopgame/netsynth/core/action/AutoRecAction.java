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
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sound.midi.Receiver;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jp.desktopgame.netsynth.NetSynth;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.AutoRecDialog;
import jp.desktopgame.netsynth.midi.MidiDeviceController;
import jp.desktopgame.netsynth.midi.MidiDeviceManager;
import jp.desktopgame.netsynth.mixer.DataLineConnection;
import jp.desktopgame.pec.PropertyEditorDialog;

/**
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
        new AutoRecDialog(this).showDialog();
    }

}
