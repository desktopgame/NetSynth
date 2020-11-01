/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jp.desktopgame.netsynth.core.GlobalSetting;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.netsynth.core.project.ProjectSettingEvent;
import jp.desktopgame.netsynth.core.project.ProjectSettingEventType;
import jp.desktopgame.netsynth.midi.MidiMainPlayer;
import jp.desktopgame.netsynth.midi.MidiPlayerDependency;
import jp.desktopgame.netsynth.midi.MidiPlayerSetting;
import jp.desktopgame.netsynth.midi.VirtualMidiEvent;
import jp.desktopgame.netsynth.midi.VirtualMidiListener;
import jp.desktopgame.netsynth.midi.VirtualMidiSequencer;
import jp.desktopgame.prc.BeatEvent;
import jp.desktopgame.prc.BeatEventType;
import jp.desktopgame.prc.KeyEvent;
import jp.desktopgame.prc.MIDI;
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

/**
 * ユーザが作業するために表示される編集機能を持った領域です.
 *
 * @author desktopgame
 */
public class WorkAreaPane extends JPanel {

    private JList<TrackSetting> trackList;
    private DefaultListModel<TrackSetting> trackListModel;
    private JScrollPane trackListScroll;
    private JTabbedPane tabbedPane;
    private Container toolBar;
    private MidiMainPlayer<PianoRollModel> midiPlayer;
    private PianoRollGroup pGroup;

    private AddTrackAction addTrackAction;
    private RemoveTrackAction removeTrackAction;

    public WorkAreaPane() {
        super(new BorderLayout());
        this.trackList = new JList<TrackSetting>((this.trackListModel = new DefaultListModel<>()));
        this.trackListScroll = new JScrollPane(trackList);
        this.tabbedPane = new JTabbedPane();
        this.toolBar = Box.createHorizontalBox();
        this.midiPlayer = new MidiMainPlayer<>((model, channel, timebase, bpm, beatWidth) -> {
            int velocity = getTrackSetting(getIndexFromPianoRollModel(model)).getVelocity();
            return MIDI.pianoRollModelToMidiEvents(model, channel, timebase, velocity, beatWidth);
        }, 480, 120, GlobalSetting.Context.getGlobalSetting().getBeatWidth());
        this.pGroup = new PianoRollGroup();
        trackList.setCellRenderer(new TrackListCellRenderer(pGroup));
        ProjectSetting.Context.getInstance().addProjectSettingListener(this::projectUpdate);
        ProjectSetting.Context.getInstance().addPropertyChangeListener(this::projectPropertyChanged);
        GlobalSetting.Context.getInstance().addPropertyChangeListener(this::globalPropertyChanged);
        tabbedPane.addChangeListener(new TabChangeHandler());
        toolBar.add(new JButton(this.addTrackAction = new AddTrackAction()));
        toolBar.add(new JButton(this.removeTrackAction = new RemoveTrackAction()));
        toolBar.add(Box.createHorizontalGlue());
        trackList.addListSelectionListener(new ListSelectionHandler());
        trackList.setFixedCellWidth(120);
        trackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(toolBar, BorderLayout.NORTH);
        add(trackListScroll, BorderLayout.WEST);
        add(tabbedPane, BorderLayout.CENTER);
    }

    //
    // イベント
    //
    public void addTrackChangeListener(TrackChangeListener listener) {
        listenerList.add(TrackChangeListener.class, listener);
    }

    protected void fireTrackChange() {
        TrackChangeEvent e = new TrackChangeEvent(this);
        for (TrackChangeListener listener : listenerList.getListeners(TrackChangeListener.class)) {
            listener.trackChange(e);
        }
    }

    public void removeTrackChangeListener(TrackChangeListener listener) {
        listenerList.remove(TrackChangeListener.class, listener);
    }

    //
    // 再生
    //
    private void setupMidiPlayer() {
        midiPlayer.clearDependency();
        for (int i = 0; i < getTrackCount(); i++) {
            TrackSetting ts = ProjectSetting.Context.getProjectSetting().getTrackSetting(i);
            PianoRollEditorPane editor = getEditor(i);
            PianoRollModel model = editor.getPianoRoll().getModel();
            VirtualMidiSequencer vseq = new RealtimeMidiSequencer(editor.getPianoRollLayerUI(), ts);
            MidiPlayerSetting setting = new MidiPlayerSetting(vseq, ts.isMute(), ts.isDrum(), ts.getBank(), ts.getProgram(), true);
            MidiPlayerDependency<PianoRollModel> dep = new MidiPlayerDependency<>(ts.getSynthesizer(), model, setting);
            midiPlayer.addDependency(dep);
        }
        midiPlayer.setup();
    }

    public void playSequence() {
        setupMidiPlayer();
        for (int i = 0; i < getTrackCount(); i++) {
            PianoRollEditorPane editor = getEditor(i);
            editor.getPianoRollLayerUI().playSequence();
            editor.getPianoRollLayerUI().setSyncScrollPane(true);
        }
    }

    public void pauseSequence() {
        for (int i = 0; i < getTrackCount(); i++) {
            getEditor(i).getPianoRollLayerUI().stopSequence();
            getEditor(i).getPianoRollLayerUI().setSyncScrollPane(false);
        }
    }

    public void resumeSequence() {
        for (int i = 0; i < getTrackCount(); i++) {
            getEditor(i).getPianoRollLayerUI().playSequence();
            getEditor(i).getPianoRollLayerUI().setSyncScrollPane(false);
        }
    }

    public void stopSequence() {
        for (int i = 0; i < getTrackCount(); i++) {
            getEditor(i).getPianoRollLayerUI().stopSequence();
            getEditor(i).getPianoRollLayerUI().setSequencePosition(0);
            getEditor(i).getPianoRollLayerUI().setSyncScrollPane(false);
        }
    }

    //
    // プロパティ
    //
    public int getSelectedTrackIndex() {
        return trackList.getSelectedIndex();
    }

    public PianoRollEditorPane getSelectedEditor() {
        return getEditor(getSelectedTrackIndex());
    }

    public TrackSetting getSelectedTrackSetting() {
        return getTrackSetting(getSelectedTrackIndex());
    }

    public List<PianoRollEditorPane> getAllEditor() {
        List<PianoRollEditorPane> r = new ArrayList<>();
        for (int i = 0; i < getTrackCount(); i++) {
            r.add(getEditor(i));
        }
        return r;
    }

    public List<TrackSetting> getAllTrackSetting() {
        List<TrackSetting> r = new ArrayList<>();
        for (int i = 0; i < getTrackCount(); i++) {
            r.add(getTrackSetting(i));
        }
        return r;
    }

    public PianoRollEditorPane getEditor(int i) {
        return (PianoRollEditorPane) tabbedPane.getComponentAt(i);
    }

    public TrackSetting getTrackSetting(int i) {
        return ProjectSetting.Context.getProjectSetting().getTrackSetting(i);
    }

    public int getTrackCount() {
        return ProjectSetting.Context.getProjectSetting().getGUITrackSettingCount();
    }

    /**
     * 指定のエディターを現在のグローバル設定と同期します.
     *
     * @param editor
     */
    public void syncGlobalSetting(PianoRollEditorPane editor) {
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

    private int getIndexFromPianoRollModel(PianoRollModel model) {
        int i = -1;
        for (int index = 0; index < getTrackCount(); index++) {
            PianoRollEditorPane editor = getEditor(index);
            if (editor.getPianoRoll().getModel().equals(model)) {
                i = index;
                break;
            }
        }
        return i;
    }

    private void projectUpdate(ProjectSettingEvent e) {
        int i = e.getIndex();
        if (e.getType() == ProjectSettingEventType.TRACK_ADDED) {
            int tabs = tabbedPane.getTabCount();
            PianoRollEditorPane editor = new PianoRollEditorPane();
            editor.getPianoRollLayerUI().setBarStyle(PianoRollLayerUI.BarStyle.PlayOneShot);
            TrackSetting tSetting = e.getSource().getTrackSetting(i);
            if (tSetting.getName().equals("Track")) {
                tSetting.setName("Track." + tabs);
            }
            tSetting.addPropertyChangeListener((pe) -> {
                if (pe.getPropertyName().equals("name")) {
                    int index = trackListModel.indexOf(tSetting);
                    trackList.repaint();
                    tabbedPane.setTitleAt(index, tSetting.getName());
                } else if (pe.getPropertyName().equals("isDrum")) {
                    setupMidiPlayer();
                    editor.getKeyboard().setUseDrumMap((boolean) pe.getNewValue());
                } else if (pe.getPropertyName().equals("isMute")) {
                    setupMidiPlayer();
                }
            });
            ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
            if (tSetting.getModel() == null) {
                ps.getTrackSetting(i).setModel(editor.getPianoRoll().getModel());
            } else {
                editor.getPianoRoll().setModel(tSetting.getModel());
            }
            editor.getPianoRoll().getModel().addPianoRollModelListener((pe) -> onModelUpdate(i, pe));
            syncGlobalSetting(editor);
            pGroup.addPianoRoll(editor.getPianoRoll());
            tabbedPane.addTab(tSetting.getName(), editor);
            trackListModel.addElement(tSetting);
            trackList.setSelectedIndex(trackListModel.getSize() - 1);
            removeTrackAction.setEnabled(true);
            setupMidiPlayer();
            fireTrackChange();
        } else if (e.getType() == ProjectSettingEventType.TRACK_REMOVED) {
            setupMidiPlayer();
            pGroup.removePianoRoll(i);
            tabbedPane.remove(i);
            trackListModel.removeElementAt(i);
        }
    }

    private void onModelUpdate(int i, PianoRollModelEvent pe) {
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
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                PianoRollModel pModel = note.getBeat().getMeasure().getKey().getModel();
                listener.virtualPlay(new VirtualMidiEvent("", (pModel.getKeyCount() - note.getBeat().getMeasure().getKey().getIndex()), 100, true));
                Thread.sleep(500);
                listener.virtualPlay(new VirtualMidiEvent("", (pModel.getKeyCount() - note.getBeat().getMeasure().getKey().getIndex()), 100, false));
                return null;
            }
        }.execute();
    }

    private void projectPropertyChanged(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        Object o = evt.getOldValue();
        Object n = evt.getNewValue();
        ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
        if (name.equals("keyMaxHeight")) {
            getAllEditor().stream().map((e) -> e.getPianoRoll()).forEach((p) -> {
                p.getModel().resizeKeyCount((int) n);
            });
        } else if (name.equals("measureMaxCount")) {
            getAllEditor().stream().map((e) -> e.getPianoRoll()).forEach((p) -> {
                p.getModel().resizeMeasureCount((int) n);
            });
        } else if (name.equals("timebase") || name.equals("bpm")) {
            getAllEditor().stream().forEach((editor) -> {
                editor.getPianoRollLayerUI().setSequenceUpdateRate(UpdateRate.bpmToUpdateRate(ps.getTimebase(), ps.getBPM()));
            });
            midiPlayer.setTimebase(ps.getTimebase());
            midiPlayer.setBPM(ps.getBPM());
        }
    }

    private void globalPropertyChanged(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        Object o = evt.getOldValue();
        Object n = evt.getNewValue();
        if (name.equals("beatWidth")) {
            getAllEditor().stream().map((e) -> e.getPianoRoll()).forEach((p) -> {
                p.setBeatWidth((int) n);
            });
            midiPlayer.setBeatWidth((int) n);
        } else if (name.equals("beatHeight")) {
            getAllEditor().stream().map((e) -> e.getPianoRoll()).forEach((p) -> {
                p.setBeatHeight((int) n);
            });
        } else if (name.equals("beatSplitCount")) {
            getAllEditor().stream().map((e) -> e.getPianoRoll()).forEach((p) -> {
                p.setBeatSplitCount((int) n);
            });
        }
    }

    //
    // アクション
    //
    private class AddTrackAction extends AbstractAction {

        public AddTrackAction() {
            super("+");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            ProjectSetting.Context.getProjectSetting().addTrackSetting(new TrackSetting());
        }

    }

    private class RemoveTrackAction extends AbstractAction {

        public RemoveTrackAction() {
            super("-");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            int i = trackList.getSelectedIndex();
            if (i >= 0) {
                ProjectSetting.Context.getProjectSetting().removeTrackSetting(i);
                setEnabled(!trackListModel.isEmpty());
            }
        }

    }

    //
    // イベント
    //
    private class ListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent arg0) {
            int i = trackList.getSelectedIndex();
            if (i >= 0) {
                pGroup.setSyncOwner(i);
                tabbedPane.setSelectedIndex(i);
                fireTrackChange();
            }
        }

    }

    private class TabChangeHandler implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent arg0) {
            if (tabbedPane.getSelectedIndex() != trackList.getSelectedIndex()) {
                trackList.setSelectedIndex(tabbedPane.getSelectedIndex());
            }
        }

    }
}
