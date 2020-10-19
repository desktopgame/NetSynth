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
import javax.swing.JOptionPane;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.GlobalSetting;
import jp.desktopgame.netsynth.core.PhraseList;
import jp.desktopgame.prc.PianoRollEditorPane;

/**
 *
 * @author desktopgame
 */
public class CopyPhraseAction extends EditorAction {

    public CopyPhraseAction(View view) {
        super(view);
        putValue(NAME, "フレーズをコピー");
    }

    @Override
    public void actionPerformed(ActionEvent e, PianoRollEditorPane editor) {
        String name = JOptionPane.showInputDialog(null, "フレーズの名前を入力してください(省略時は最後のフレーズ)");
        if (name == null || name.equals("")) {
            name = "";
        }
        final String fname = name;
        GlobalSetting gs = GlobalSetting.Context.getGlobalSetting();
        PhraseList pl = gs.getPhraseList();
        if (pl.isEmpty()) {
            return;
        }
        if (name.equals("")) {
            editor.getPianoRoll().copy(pl.get(pl.size() - 1));
        } else {
            pl.stream().filter((ph) -> ph.getName().equals(fname)).findFirst().ifPresent(editor.getPianoRoll()::copy);
        }
    }
}
