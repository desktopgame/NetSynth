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
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import static jp.desktopgame.netsynth.NetSynth.logException;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.GlobalSetting;
import jp.desktopgame.prc.Note;
import jp.desktopgame.prc.Phrase;
import jp.desktopgame.prc.PianoRollEditorPane;

/**
 *
 * @author desktopgame
 */
public class SavePhraseAction extends EditorAction {

    public SavePhraseAction(View view) {
        super(view);
        putValue(NAME, "選択範囲をフレーズとして保存");
    }

    @Override
    public void actionPerformed(ActionEvent e, PianoRollEditorPane editor) {
        String name = JOptionPane.showInputDialog(null, "フレーズの名前を入力してください");
        if (name == null || name.equals("")) {
            name = "Untitled";
        }
        GlobalSetting gs = GlobalSetting.Context.getGlobalSetting();
        List<Note> selected = editor.getPianoRoll().getModel().getSelectedNotes();
        Phrase ph = Phrase.createFromNotes(selected);
        ph.setName(name);
        gs.getPhraseList().add(ph);
        selected.forEach((n) -> n.setSelected(false));
        try {
            GlobalSetting.Context.getInstance().save();
        } catch (IOException ex) {
            logException(ex);
        }
    }

}
