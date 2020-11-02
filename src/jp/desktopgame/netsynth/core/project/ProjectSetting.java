/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.project;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.event.EventListenerList;
import jp.desktopgame.netsynth.core.PianoRollModelTypeAdapter;
import jp.desktopgame.pec.GetMethod;
import jp.desktopgame.pec.Property;
import jp.desktopgame.pec.SetMethod;
import jp.desktopgame.prc.PianoRollModel;

/**
 * プロジェクトの設定情報.
 *
 * @author desktopgame
 */
public class ProjectSetting {

    @Expose
    @Property("名前")
    @SetMethod
    @GetMethod
    private String name;
    @Expose
    @Property("タイムベース")
    @SetMethod
    @GetMethod
    private int timebase;
    @Expose
    @Property("BPM")
    @SetMethod("setBPM")
    @GetMethod("getBPM")
    private int bpm;
    @Expose
    @Property("キーの高さ")
    @SetMethod
    @GetMethod
    private int keyMaxHeight;
    @Expose
    @Property("小節の数")
    @SetMethod
    @GetMethod
    private int measureMaxCount;
    @Expose
    private List<TrackSetting> trackSettingList;

    private EventListenerList listenerList;
    private PropertyChangeSupport support;
    private boolean modified;
    private PropertyChangeHandler propertyChangeHandler;

    public ProjectSetting() {
        this.name = "Untitled";
        this.timebase = 480;
        this.bpm = 120;
        this.keyMaxHeight = 12 * 11;
        this.measureMaxCount = 4;
        this.trackSettingList = new ArrayList<>();
        this.listenerList = new EventListenerList();
        this.modified = false;
        this.support = new PropertyChangeSupport(this);
        this.propertyChangeHandler = new PropertyChangeHandler();
    }

    private void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    private void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private void addProjectSettingListener(ProjectSettingListener listener) {
        listenerList.add(ProjectSettingListener.class, listener);
    }

    private void removeProjectSettingListener(ProjectSettingListener listener) {
        listenerList.remove(ProjectSettingListener.class, listener);
    }

    protected void fireProjectUpdate(ProjectSettingEvent e) {
        for (ProjectSettingListener listener : listenerList.getListeners(ProjectSettingListener.class)) {
            listener.projectUpdate(e);
        }
    }

    private void addProjectModifyListener(ProjectModifyListener listener) {
        listenerList.add(ProjectModifyListener.class, listener);
    }

    private void removeProjectModifyListener(ProjectModifyListener listener) {
        listenerList.remove(ProjectModifyListener.class, listener);
    }

    protected void fireProjectModified(ProjectModifyEvent e) {
        for (ProjectModifyListener listener : listenerList.getListeners(ProjectModifyListener.class)) {
            listener.projectModified(e);
        }
    }

    public void clear() {
        this.modified = false;
        fireProjectModified(new ProjectModifyEvent(this, ProjectModifyEventType.CLEAR));
    }

    public void modify() {
        this.modified = true;
        fireProjectModified(new ProjectModifyEvent(this, ProjectModifyEventType.MODIFY));
    }

    public void save() {
        this.modified = false;
        fireProjectModified(new ProjectModifyEvent(this, ProjectModifyEventType.SAVED));
    }

    public boolean isModified() {
        return modified;
    }

    public void setName(String name) {
        String a = this.name;
        this.name = name;
        support.firePropertyChange("name", a, name);
        modify();
    }

    public String getName() {
        return name;
    }

    public void setTimebase(int timebase) {
        int a = this.timebase;
        this.timebase = timebase;
        support.firePropertyChange("timebase", a, timebase);
        modify();
    }

    public int getTimebase() {
        return timebase;
    }

    public void setBPM(int bpm) {
        int a = this.bpm;
        this.bpm = bpm;
        support.firePropertyChange("bpm", a, bpm);
        modify();
    }

    public int getBPM() {
        return bpm;
    }

    public void setKeyMaxHeight(int keyMaxHeight) {
        int a = this.keyMaxHeight;
        this.keyMaxHeight = keyMaxHeight;
        support.firePropertyChange("keyMaxHeight", a, keyMaxHeight);
        modify();
    }

    public int getKeyMaxHeight() {
        return keyMaxHeight;
    }

    public void setMeasureMaxCount(int measureMaxCount) {
        int a = this.measureMaxCount;
        this.measureMaxCount = measureMaxCount;
        support.firePropertyChange("measureMaxCount", a, measureMaxCount);
        modify();
    }

    public int getMeasureMaxCount() {
        return measureMaxCount;
    }

    public void addTrackSetting(TrackSetting trackSetting) {
        trackSettingList.add(trackSetting);
        trackSetting.setInternal(false);
        trackSetting.addPropertyChangeListener(propertyChangeHandler);
        modify();
        fireProjectUpdate(new ProjectSettingEvent(this, ProjectSettingEventType.TRACK_ADDED, trackSettingList.size() - 1));
    }

    public void removeTrackSetting(int i) {
        trackSettingList.get(i).removePropertyChangeListener(propertyChangeHandler);
        trackSettingList.remove(i);
        modify();
        fireProjectUpdate(new ProjectSettingEvent(this, ProjectSettingEventType.TRACK_REMOVED, i));
    }

    public void removeAllTrackSetting() {
        while (!trackSettingList.isEmpty()) {
            removeTrackSetting(0);
        }
    }

    public Optional<TrackSetting> getTrackSetting(String uuid) {
        return trackSettingList.stream().filter((e) -> e.getUUID().equals(uuid)).findFirst();
    }

    public TrackSetting getTrackSetting(int i) {
        return trackSettingList.get(i);
    }

    public int getTrackSettingCount() {
        return trackSettingList.size();
    }

    public int getGUITrackSettingCount() {
        return (int) trackSettingList.stream().filter((e) -> !e.isInternal()).count();
    }

    public static class Context {

        private static Context instance;
        private ProjectSetting setting;
        private Optional<String> filePath;
        private List<PropertyChangeListener> propChangeListeners;
        private List<ProjectSettingListener> projectSettingListeners;
        private List<ProjectModifyListener> projectModifyListeners;

        private Context() {
            this.setting = new ProjectSetting();
            this.filePath = Optional.empty();
            this.propChangeListeners = new ArrayList<>();
            this.projectSettingListeners = new ArrayList<>();
            this.projectModifyListeners = new ArrayList<>();
        }

        public static Context getInstance() {
            if (instance == null) {
                instance = new Context();
            }
            return instance;
        }

        public static ProjectSetting getProjectSetting() {
            return instance.setting;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propChangeListeners.add(listener);
            setting.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propChangeListeners.remove(listener);
            setting.removePropertyChangeListener(listener);
        }

        public void addProjectSettingListener(ProjectSettingListener listener) {
            projectSettingListeners.add(listener);
            setting.addProjectSettingListener(listener);
        }

        public void removeProjectSettingListener(ProjectSettingListener listener) {
            projectSettingListeners.remove(listener);
            setting.removeProjectSettingListener(listener);
        }

        public void addProjectModifyListener(ProjectModifyListener listener) {
            projectModifyListeners.add(listener);
            setting.addProjectModifyListener(listener);
        }

        public void removeProjectModifyListener(ProjectModifyListener listener) {
            projectModifyListeners.remove(listener);
            setting.removeProjectModifyListener(listener);
        }

        public void clear() {
            setting.removeAllTrackSetting();
            this.setting = new ProjectSetting();
            propChangeListeners.forEach(setting::addPropertyChangeListener);
            projectSettingListeners.forEach(setting::addProjectSettingListener);
            projectModifyListeners.forEach(setting::addProjectModifyListener);
            this.filePath = Optional.empty();
            setting.clear();
        }

        public void open(File file) throws FileNotFoundException {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(PianoRollModel.class, new PianoRollModelTypeAdapter())
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
            setting.removeAllTrackSetting();
            this.setting = gson.fromJson(new FileReader(file), ProjectSetting.class);
            propChangeListeners.forEach(setting::addPropertyChangeListener);
            projectSettingListeners.forEach(setting::addProjectSettingListener);
            projectModifyListeners.forEach(setting::addProjectModifyListener);
            this.filePath = Optional.of(file.getPath());
            for (int i = 0; i < setting.getTrackSettingCount(); i++) {
                TrackSetting ts = setting.getTrackSetting(i);
                ts.setInternal(false);
                ts.addPropertyChangeListener((e) -> setting.modify());
                ProjectSettingEvent e = new ProjectSettingEvent(setting, ProjectSettingEventType.TRACK_ADDED, i);
                for (ProjectSettingListener listener : projectSettingListeners) {
                    listener.projectUpdate(e);
                }
            }
            setting.clear();
        }

        public void open(String file) throws FileNotFoundException {
            open(new File(file));
        }

        public void save() throws IOException {
            saveAs(filePath.get());
        }

        public void saveAs(String file) throws IOException {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(PianoRollModel.class, new PianoRollModelTypeAdapter())
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
            try ( FileWriter fw = new FileWriter(file)) {
                fw.write(gson.toJson(setting));
            }
            this.filePath = Optional.of(file);
            setting.save();
        }

        public ProjectSetting getSetting() {
            return setting;
        }

        public void setFilePath(Optional<String> filePath) {
            this.filePath = filePath;
        }

        public Optional<String> getFilePath() {
            return filePath;
        }
    }

    private class PropertyChangeHandler implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent arg0) {
            modify();
        }
    }
}
