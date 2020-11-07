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
import java.io.IOException;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import static jp.desktopgame.netsynth.NetSynth.logException;
import static jp.desktopgame.netsynth.NetSynth.logInformation;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.GlobalSetting;
import jp.desktopgame.netsynth.core.editor.WorkAreaPane;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.netsynth.core.project.TrackSetting;
import jp.desktopgame.netsynth.resources.Resources;
import jp.desktopgame.prc.MIDI;
import jp.desktopgame.prc.PianoRollEditorPane;
import jp.desktopgame.prc.PianoRollModel;

/**
 *
 * @author desktopgame
 */
public class ExportAction extends ViewAction {

    public ExportAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "エクスポート");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Export16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Export24")));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        ProjectSetting.Context pctx = ProjectSetting.Context.getInstance();
        if (!pctx.getFilePath().isPresent()) {
            view.getAction("SaveAsAction").actionPerformed(arg0);
        }
        GlobalSetting gs = GlobalSetting.Context.getGlobalSetting();
        ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
        try {
            Sequence seq = new Sequence(Sequence.PPQ, ps.getTimebase());
            WorkAreaPane wp = view.getWorkAreaPane();
            for (int i = 0; i < wp.getTrackCount(); i++) {
                TrackSetting ts = wp.getTrackSetting(i);
                PianoRollEditorPane editor = wp.getTrackEditor(i);
                PianoRollModel model = editor.getPianoRoll().getModel();
                List<MidiEvent> events = MIDI.pianoRollModelToMidiEvents(model, i, ps.getTimebase(), ts.getVelocity(), gs.getBeatWidth());
                Track t = seq.createTrack();
                events.forEach(t::add);
            }
            File file = new File(ps.getName() + ".mid");
            MidiSystem.write(seq, gs.getMidiFileType(), file);
            logInformation(file.getPath() + "が作成されました。");
        } catch (InvalidMidiDataException | IOException ex) {
            logException(ex);
        }
    }

}
