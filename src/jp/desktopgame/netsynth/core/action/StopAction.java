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
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.editor.TrackChangeEvent;
import jp.desktopgame.netsynth.resources.Resources;
import jp.desktopgame.prc.PianoRollEditorPane;
import jp.desktopgame.prc.SequenceEvent;
import jp.desktopgame.prc.SequenceListener;

/**
 *
 * @author desktopgame
 */
public class StopAction extends EditorAction implements SequenceListener {

    private PianoRollEditorPane editor;

    public StopAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "停止");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "Stop16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.Media, "Stop24")));
        setEnabled(false);
    }

    @Override
    protected boolean canExec(int newEditorIndex) {
        return false;
    }

    @Override
    protected void trackChange(TrackChangeEvent e) {
        super.trackChange(e); //To change body of generated methods, choose Tools | Templates.
        int i = view.getWorkAreaPane().getSelectedTrackIndex();
        if (editor != null) {
            editor.getPianoRollLayerUI().removeSequenceListener(this);
        }
        if (i < 0) {
            return;
        }
        this.editor = view.getWorkAreaPane().getTrackEditor(i);
        editor.getPianoRollLayerUI().addSequenceListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e, PianoRollEditorPane editor) {
        view.getWorkAreaPane().getTrackEditorManager().stopSequence();
        editor.getPianoRollLayerUI().setSequencePosition(0);
        view.getAction("PlayAction").setEnabled(true);
        view.getAction("PauseAction").setEnabled(false);
        setEnabled(false);
    }

    @Override
    public void sequenceUpdate(SequenceEvent e) {
        if (isEnabled() && e.getType() == SequenceEvent.Type.Reset) {
            setEnabled(false);
            view.getAction("PauseAction").setEnabled(false);
            view.getAction("PlayAction").setEnabled(true);
        }
    }

}
