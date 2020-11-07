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
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.editor.TrackChangeEvent;
import jp.desktopgame.netsynth.resources.Resources;
import jp.desktopgame.prc.PianoRollEditorPane;
import jp.desktopgame.prc.PianoRollModelEvent;
import jp.desktopgame.prc.PianoRollModelListener;

/**
 *
 * @author desktopgame
 */
public class UndoAction extends EditorAction implements PianoRollModelListener {

    private PianoRollEditorPane editor;

    public UndoAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "元に戻す");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Undo16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Undo24")));
        putValue(AbstractAction.MNEMONIC_KEY, (int) 'Z');
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
        setEnabled(false);
    }

    @Override
    protected void trackChange(TrackChangeEvent e) {
        super.trackChange(e); //To change body of generated methods, choose Tools | Templates.
        int i = view.getWorkAreaPane().getSelectedTrackIndex();
        if (editor != null) {
            editor.getPianoRoll().getModel().removePianoRollModelListener(this);
        }
        if (i >= 0) {
            PianoRollEditorPane newEditor = view.getWorkAreaPane().getTrackEditor(i);
            newEditor.getPianoRoll().getModel().addPianoRollModelListener(this);
            this.editor = newEditor;
        }
    }

    @Override
    protected boolean canExec(int newEditorIndex) {
        return view.getWorkAreaPane().getTrackEditor(newEditorIndex).getUndoManager().canUndo();
    }

    @Override
    public void actionPerformed(ActionEvent e, PianoRollEditorPane editor) {
        UndoManager um = editor.getUndoManager();
        if (um.canUndo()) {
            um.undo();
        }
        setEnabled(um.canUndo());
    }

    @Override
    public void pianoRollModelUpdate(PianoRollModelEvent e) {
        setEnabled(editor.getUndoManager().canUndo());
    }

}
