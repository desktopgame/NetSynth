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
import jp.desktopgame.netsynth.core.editor.TrackChangeEvent;
import jp.desktopgame.prc.PianoRollEditorPane;

/**
 *
 * @author desktopgame
 */
public class EditorAction extends ViewAction {

    public EditorAction(View view) {
        super(view);
        view.getWorkAreaPane().addTrackChangeListener(this::trackChange);
    }

    /**
     * トラックが変更されると呼び出されます.
     *
     * @param e
     */
    protected void trackChange(TrackChangeEvent e) {
        int i = view.getWorkAreaPane().getSelectedTrackIndex();
        if (i < 0) {
            setEnabled(false);
        } else {
            setEnabled(canExec(i));
        }
    }

    /**
     * トラックが選択された時にこのアクションを実行可能かどうかを決定するために呼び出されます.
     *
     * @param newEditorIndex
     * @return
     */
    protected boolean canExec(int newEditorIndex) {
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        PianoRollEditorPane ed = view.getWorkAreaPane().getSelectedEditor();
        if (ed != null) {
            actionPerformed(arg0, ed);
        }
    }

    public void actionPerformed(ActionEvent e, PianoRollEditorPane editor) {
    }
}
