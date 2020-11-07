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
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.netsynth.core.project.ProjectSettingEvent;
import jp.desktopgame.netsynth.core.project.ProjectSettingEventType;
import jp.desktopgame.netsynth.core.project.TrackSetting;
import jp.desktopgame.prc.PianoRollEditorPane;

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
    private TrackEditorManager editorManager;

    private AddTrackAction addTrackAction;
    private RemoveTrackAction removeTrackAction;
    private JLabel chordLabel;

    public WorkAreaPane() {
        super(new BorderLayout());
        this.trackList = new JList<TrackSetting>((this.trackListModel = new DefaultListModel<>()));
        this.trackListScroll = new JScrollPane(trackList);
        this.tabbedPane = new JTabbedPane();
        this.toolBar = Box.createHorizontalBox();
        this.editorManager = new TrackEditorManager();
        this.chordLabel = new JLabel("");
        trackList.setCellRenderer(new TrackListCellRenderer(editorManager.getPianoRollGroup()));
        ProjectSetting.Context.getInstance().addProjectSettingListener(this::projectUpdate);
        tabbedPane.addChangeListener(new TabChangeHandler());
        toolBar.add(new JButton(this.addTrackAction = new AddTrackAction()));
        toolBar.add(new JButton(this.removeTrackAction = new RemoveTrackAction()));
        toolBar.add(chordLabel);
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
        for (int i = 0; i < getTrackCount(); i++) {
            editorManager.getEditor(i).getKeyboard().resetHighlight();
        }
        TrackChangeEvent e = new TrackChangeEvent(this);
        for (TrackChangeListener listener : listenerList.getListeners(TrackChangeListener.class)) {
            listener.trackChange(e);
        }
    }

    public void removeTrackChangeListener(TrackChangeListener listener) {
        listenerList.remove(TrackChangeListener.class, listener);
    }

    public void showChordLabel(String label) {
        chordLabel.setText(label);
    }

    //
    // プロパティ
    //
    public TrackEditorManager getTrackEditorManager() {
        return editorManager;
    }

    public int getSelectedTrackIndex() {
        return trackList.getSelectedIndex();
    }

    public PianoRollEditorPane getSelectedEditor() {
        return editorManager.getEditor(getSelectedTrackIndex());
    }

    public TrackSetting getSelectedTrackSetting() {
        return getTrackSetting(getSelectedTrackIndex());
    }

    public List<PianoRollEditorPane> getAllTrackEditor() {
        List<PianoRollEditorPane> r = new ArrayList<>();
        for (int i = 0; i < getTrackCount(); i++) {
            r.add(getTrackEditor(i));
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

    public PianoRollEditorPane getTrackEditor(int i) {
        return editorManager.getEditor(i);
    }

    public TrackSetting getTrackSetting(int i) {
        return ProjectSetting.Context.getProjectSetting().getTrackSetting(i);
    }

    public int getTrackCount() {
        return ProjectSetting.Context.getProjectSetting().getGUITrackSettingCount();
    }

    private void projectUpdate(ProjectSettingEvent e) {
        int i = e.getIndex();
        if (e.getType() == ProjectSettingEventType.TRACK_ADDED) {
            int tabs = tabbedPane.getTabCount();
            TrackSetting ts = e.getSource().getTrackSetting(i);
            if (ts.getName().equals("Track")) {
                ts.setName("Track." + tabs);
            }
            ts.addPropertyChangeListener((pe) -> {
                if (!pe.getPropertyName().equals("name")) {
                    return;
                }
                int index = trackListModel.indexOf(ts);
                trackList.repaint();
                tabbedPane.setTitleAt(index, ts.getName());
            });
            tabbedPane.addTab(ts.getName(), editorManager.addEditor(ts));
            trackListModel.addElement(ts);
            trackList.setSelectedIndex(trackListModel.getSize() - 1);
            removeTrackAction.setEnabled(true);
            fireTrackChange();
        } else if (e.getType() == ProjectSettingEventType.TRACK_REMOVED) {
            editorManager.removeEditor(i);
            tabbedPane.remove(i);
            trackListModel.removeElementAt(i);
            fireTrackChange();
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
                editorManager.getPianoRollGroup().setSyncOwner(i);
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
