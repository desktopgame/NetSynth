/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import jp.desktopgame.netsynth.NetSynth;
import jp.desktopgame.netsynth.core.editor.WorkAreaPane;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.netsynth.core.project.TrackSetting;
import jp.desktopgame.pec.builder.ComboBoxHelper;
import jp.desktopgame.pec.builder.IntegerSpinnerHelper;
import jp.desktopgame.pec.builder.PropertyEditorBuilder;
import jp.desktopgame.prc.Key;
import jp.desktopgame.prc.MIDI;
import jp.desktopgame.prc.Note;
import jp.desktopgame.prc.PianoRoll;
import jp.desktopgame.prc.PianoRollModel;

/**
 *
 * @author desktopgame
 */
public class AutoGeneratePane extends JPanel {

    private WorkAreaPane wp;

    private ComboBoxHelper<String> drumASound;
    private IntegerSpinnerHelper drumAAmount;
    private IntegerSpinnerHelper drumAMergin;

    private ComboBoxHelper<String> drumBSound;
    private IntegerSpinnerHelper drumBAmount;
    private IntegerSpinnerHelper drumBMergin;

    public AutoGeneratePane() {
        super(new BorderLayout());
        PropertyEditorBuilder pb = new PropertyEditorBuilder();
        pb.separator("コード進行");
        pb.textField("A");
        pb.button("生成").onPush(this::generateChord);
        pb.separator("ドラム");
        drumASound = pb.comboBox("音A").overwrite(MIDI.DRUM_RMAP.keySet().stream().collect(Collectors.toList()));
        drumAAmount = pb.intSpinner("連続数").range(1, 1, 8, 1);
        drumAMergin = pb.intSpinner("余白").range(1, 1, 8, 1);
        drumBSound = pb.comboBox("音B").overwrite(MIDI.DRUM_RMAP.keySet().stream().collect(Collectors.toList()));
        drumBAmount = pb.intSpinner("連続数").range(1, 1, 8, 1);
        drumBMergin = pb.intSpinner("余白").range(1, 1, 8, 1);;
        pb.button("生成").onPush(this::generateDrum);
        add(pb.buildPane(), BorderLayout.CENTER);
    }

    private boolean canGenerate() {
        if (wp == null) {
            this.wp = NetSynth.getView().getWorkAreaPane();
        }
        int i = wp.getSelectedTrackIndex();
        if (i < 0) {
            return false;
        }
        ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
        if (!ps.getTrackSetting(i).isAutoGenerate()) {
            return false;
        }
        return true;
    }

    private void removeAllBeats(PianoRollModel pModel) {
        List<Note> notes = pModel.getAllNotes();
        if (notes.isEmpty()) {
            return;
        }
        pModel.beginCompoundUndoableEdit();
        notes.forEach((n) -> n.removeFromBeat());
        pModel.endCompoundUndoableEdit();
    }

    private void generateChord(ActionEvent e) {
        if (!canGenerate()) {
            NetSynth.logInformation("トラックが自動生成を許可していないか、トラックがありません。");
            return;
        }
        PianoRollModel pModel = NetSynth.getView().getWorkAreaPane().getSelectedEditor().getPianoRoll().getModel();
        removeAllBeats(pModel);
    }

    private void generateDrum(ActionEvent e) {
        if (!canGenerate()) {
            NetSynth.logInformation("トラックが自動生成を許可していないか、トラックがありません。");
            return;
        }
        int i = wp.getSelectedTrackIndex();
        TrackSetting ts = ProjectSetting.Context.getProjectSetting().getTrackSetting(i);
        if (!ts.isDrum()) {
            NetSynth.logInformation("トラックがドラム用ではありません。");
            return;
        }
        boolean mute = ts.isMute();
        ts.setMute(true);
        PianoRoll p = NetSynth.getView().getWorkAreaPane().getSelectedEditor().getPianoRoll();
        PianoRollModel pModel = p.getModel();
        removeAllBeats(pModel);
        String drumA = drumASound.at(drumASound.index());
        String drumB = drumBSound.at(drumBSound.index());
        int pos = 0;
        int stat = 0;
        int endPos = pModel.getKey(0).getMeasureCount() * (pModel.getKey(0).getMeasure(0).getBeatCount() * p.getBeatSplitCount()) * p.getBeatWidth();
        while (pos < endPos) {
            int keyI = stat == 0 ? MIDI.DRUM_RMAP.get(drumA) : MIDI.DRUM_RMAP.get(drumB);
            Key key = pModel.getKey(pModel.getKeyCount() - keyI);
            int am = stat == 0 ? drumAAmount.current() : drumBAmount.current();
            int step = stat == 0 ? drumAMergin.current() : drumBMergin.current();
            for (int c = 0; c < am; c++) {
                int measureI = pos / key.getMeasure(0).getBeatCount();
                int beatI = (pos % key.getMeasure(0).getBeatCount());
                key.getMeasure(0).getBeat(0).generateNote(pos, 1.0f / (float) p.getBeatSplitCount());
                pos += step * (p.getBeatWidth() / p.getBeatSplitCount());
                if (pos >= endPos) {
                    break;
                }
            }
            stat++;
            if (stat == 2) {
                stat = 0;
            }
        }
        ts.setMute(mute);
    }
}
