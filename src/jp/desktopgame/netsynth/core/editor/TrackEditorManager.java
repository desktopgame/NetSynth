/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.editor;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jp.desktopgame.netsynth.core.GlobalSetting;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.netsynth.core.project.TrackSetting;
import jp.desktopgame.netsynth.midi.MidiMainPlayer;
import jp.desktopgame.netsynth.midi.MidiPlayerDependency;
import jp.desktopgame.netsynth.midi.MidiPlayerSetting;
import jp.desktopgame.netsynth.midi.VirtualMidiEvent;
import jp.desktopgame.netsynth.midi.VirtualMidiListener;
import jp.desktopgame.prc.BeatEvent;
import jp.desktopgame.prc.BeatEventType;
import jp.desktopgame.prc.KeyEvent;
import jp.desktopgame.prc.MeasureEvent;
import jp.desktopgame.prc.Note;
import jp.desktopgame.prc.NoteEvent;
import jp.desktopgame.prc.PianoRoll;
import jp.desktopgame.prc.PianoRollEditorPane;
import jp.desktopgame.prc.PianoRollGroup;
import jp.desktopgame.prc.PianoRollLayerUI;
import jp.desktopgame.prc.PianoRollModel;
import jp.desktopgame.prc.PianoRollModelEvent;
import jp.desktopgame.prc.UpdateRate;
import jp.desktopgame.stask.SwingTask;

/**
 *
 * @author desktopgame
 */
public class TrackEditorManager {

    private List<PianoRollEditorPane> editorList;
    private PianoRollGroup pianoRollGroup;
    private List<RealtimeMidiSequencer> sequencers;
    private MidiMainPlayer midiPlayer;

    public TrackEditorManager() {
        this.editorList = new ArrayList<>();
        this.pianoRollGroup = new PianoRollGroup();
        this.sequencers = new ArrayList<>();
        this.midiPlayer = new MidiMainPlayer();
        ProjectSetting.Context.getInstance().addPropertyChangeListener(this::projectPropertyChanged);
        GlobalSetting.Context.getInstance().addPropertyChangeListener(this::globalPropertyChanged);
    }

    //
    public void setupMidiPlayer() {
        sequencers.clear();
        midiPlayer.clearDependency();
        for (int i = 0; i < getEditorCount(); i++) {
            TrackSetting ts = ProjectSetting.Context.getProjectSetting().getTrackSetting(i);
            PianoRollEditorPane editor = getEditor(i);
            RealtimeMidiSequencer vseq = new RealtimeMidiSequencer(editor.getPianoRollLayerUI(), ts);
            MidiPlayerSetting setting = new MidiPlayerSetting(vseq, ts.isMute(), ts.isDrum(), ts.getBank(), ts.getProgram());
            MidiPlayerDependency dep = new MidiPlayerDependency(ts.getSynthesizer(), setting);
            midiPlayer.addDependency(dep);
            sequencers.add(vseq);
        }
        midiPlayer.setup();
    }

    public void playSequence() {
        setupMidiPlayer();
        for (int i = 0; i < getEditorCount(); i++) {
            PianoRollEditorPane editor = getEditor(i);
            editor.getPianoRollLayerUI().playSequence();
            editor.getPianoRollLayerUI().setSyncScrollPane(true);
        }
    }

    public void pauseSequence() {
        for (int i = 0; i < getEditorCount(); i++) {
            getEditor(i).getPianoRollLayerUI().stopSequence();
            getEditor(i).getPianoRollLayerUI().setSyncScrollPane(false);
        }
        midiPlayer.allNotesOff();
    }

    public void resumeSequence() {
        for (int i = 0; i < getEditorCount(); i++) {
            getEditor(i).getPianoRollLayerUI().playSequence();
            getEditor(i).getPianoRollLayerUI().setSyncScrollPane(false);
        }
    }

    public void stopSequence() {
        for (int i = 0; i < getEditorCount(); i++) {
            getEditor(i).getPianoRollLayerUI().stopSequence();
            getEditor(i).getPianoRollLayerUI().setSequencePosition(0);
            getEditor(i).getPianoRollLayerUI().setSyncScrollPane(false);
        }
        midiPlayer.allNotesOff();
    }

    public void noteOn(String uuid, int height, int velocity) {
        for (int i = 0; i < getEditorCount(); i++) {
            TrackSetting ts = ProjectSetting.Context.getProjectSetting().getTrackSetting(i);
            if (ts.getUUID().equals(uuid)) {
                sequencers.get(i).noteOn(height, velocity);
                break;
            }
        }
    }

    public void noteOff(String uuid, int height, int velocity) {
        for (int i = 0; i < getEditorCount(); i++) {
            TrackSetting ts = ProjectSetting.Context.getProjectSetting().getTrackSetting(i);
            if (ts.getUUID().equals(uuid)) {
                sequencers.get(i).noteOff(height);
                break;
            }
        }
    }

    public PianoRollEditorPane addEditor(TrackSetting ts) {
        //final int i = getEditorCount();
        PianoRollEditorPane editor = new PianoRollEditorPane();
        editorList.add(editor);
        editor.getPianoRollLayerUI().setBarStyle(PianoRollLayerUI.BarStyle.PlayOneShot);
        ts.addPropertyChangeListener((pe) -> {
            String pnam = pe.getPropertyName();
            if (pnam.equals("isDrum")) {
                setupMidiPlayer();
                editor.getKeyboard().setUseDrumMap((boolean) pe.getNewValue());
            } else if (pnam.equals("isMute") || pnam.equals("bank") || pnam.equals("program")) {
                setupMidiPlayer();
            } else if (pnam.equals("autoGenerate")) {
                editor.getPianoRoll().setEditable(!(boolean) pe.getNewValue());
            }
        });
        ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
        if (ts.getModel() == null) {
            ts.setModel(editor.getPianoRoll().getModel());
        } else {
            editor.getPianoRoll().setModel(ts.getModel());
        }
        editor.getPianoRoll().setEditable(!ts.isAutoGenerate());
        editor.getPianoRoll().getModel().addPianoRollModelListener((pe) -> onModelUpdate(editor, pe));
        syncGlobalSetting(editor);
        pianoRollGroup.addPianoRoll(editor.getPianoRoll());
        setupMidiPlayer();
        return editor;
    }

    public int indexForEditor(PianoRollEditorPane editor) {
        return editorList.indexOf(editor);
    }

    public void removeEditor(int i) {
        editorList.remove(i);
        setupMidiPlayer();
        pianoRollGroup.removePianoRoll(i);
    }

    public PianoRollEditorPane getEditor(int i) {
        return editorList.get(i);
    }

    public int getEditorCount() {
        return editorList.size();
    }

    public PianoRollGroup getPianoRollGroup() {
        return pianoRollGroup;
    }

    private void onModelUpdate(PianoRollEditorPane editor, PianoRollModelEvent pe) {
        final int i = indexForEditor(editor);
        Optional<NoteEvent> neOpt = pe.getNoteEvent();
        Optional<BeatEvent> beOpt = pe.getBeatEvent();
        Optional<MeasureEvent> meOpt = pe.getMeasureEvent();
        Optional<KeyEvent> keOpt = pe.getInnerEvent();
        if (!beOpt.isPresent()) {
            return;
        }
        // ノート作成時に音を鳴らす
        BeatEvent be = beOpt.get();
        if (be.getBeatEventType() != BeatEventType.NOTE_CREATED) {
            return;
        }
        midiPlayer.getDependency(i).player.ifPresent((player) -> {
            if (!(player instanceof VirtualMidiListener)) {
                return;
            }
            VirtualMidiListener vml = (VirtualMidiListener) player;
            triggerMidiEvent(vml, be.getNote());
        });
    }

    private void triggerMidiEvent(VirtualMidiListener listener, Note note) {
        SwingTask.create(() -> {
            PianoRollModel pModel = note.getBeat().getMeasure().getKey().getModel();
            listener.virtualPlay(new VirtualMidiEvent("", (pModel.getKeyCount() - note.getBeat().getMeasure().getKey().getIndex()), 100, true));
            Thread.sleep(500);
            listener.virtualPlay(new VirtualMidiEvent("", (pModel.getKeyCount() - note.getBeat().getMeasure().getKey().getIndex()), 100, false));
        }).forget();
    }

    private void syncGlobalSetting(PianoRollEditorPane editor) {
        PianoRoll p = editor.getPianoRoll();
        GlobalSetting gs = GlobalSetting.Context.getGlobalSetting();
        ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
        p.getModel().resizeKeyCount(ps.getKeyMaxHeight());
        p.getModel().resizeMeasureCount(ps.getMeasureMaxCount());
        p.setBeatWidth(gs.getBeatWidth());
        p.setBeatHeight(gs.getBeatHeight());
        p.setBeatSplitCount(gs.getBeatSplitCount());
        editor.getPianoRollLayerUI().setSequenceUpdateRate(UpdateRate.bpmToUpdateRate(ps.getTimebase(), ps.getBPM()));
    }

    private void projectPropertyChanged(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        Object o = evt.getOldValue();
        Object n = evt.getNewValue();
        ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
        if (name.equals("keyMaxHeight")) {
            editorList.stream().map((e) -> e.getPianoRoll()).forEach((p) -> {
                p.getModel().resizeKeyCount((int) n);
            });
        } else if (name.equals("measureMaxCount")) {
            editorList.stream().map((e) -> e.getPianoRoll()).forEach((p) -> {
                p.getModel().resizeMeasureCount((int) n);
            });
        } else if (name.equals("timebase") || name.equals("bpm")) {
            editorList.stream().forEach((editor) -> {
                editor.getPianoRollLayerUI().setSequenceUpdateRate(UpdateRate.bpmToUpdateRate(ps.getTimebase(), ps.getBPM()));
            });
        }
    }

    private void globalPropertyChanged(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        Object o = evt.getOldValue();
        Object n = evt.getNewValue();
        if (name.equals("beatWidth")) {
            editorList.stream().map((e) -> e.getPianoRoll()).forEach((p) -> {
                p.setBeatWidth((int) n);
            });
        } else if (name.equals("beatHeight")) {
            editorList.stream().map((e) -> e.getPianoRoll()).forEach((p) -> {
                p.setBeatHeight((int) n);
            });
        } else if (name.equals("beatSplitCount")) {
            editorList.stream().map((e) -> e.getPianoRoll()).forEach((p) -> {
                p.setBeatSplitCount((int) n);
            });
        }
    }

}
