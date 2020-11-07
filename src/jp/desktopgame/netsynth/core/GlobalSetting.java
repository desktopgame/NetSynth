/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import jp.desktopgame.netsynth.music21.PythonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jp.desktopgame.netsynth.sound.SoundDatabase;
import jp.desktopgame.pec.GetMethod;
import jp.desktopgame.pec.ListProperty;
import jp.desktopgame.pec.PathString;
import jp.desktopgame.pec.PathStringType;
import jp.desktopgame.pec.Property;
import jp.desktopgame.pec.Separator;
import jp.desktopgame.pec.SetMethod;

/**
 *
 * @author desktopgame
 */
public class GlobalSetting {

    @Expose
    @Separator("ピアノロール")
    @Property("拍の横幅")
    @SetMethod
    @GetMethod
    private int beatWidth;

    @Expose
    @Property("拍の縦幅")
    @SetMethod
    @GetMethod
    private int beatHeight;

    @Expose
    @Property("拍の分割数")
    @SetMethod
    @GetMethod
    private int beatSplitCount;

    @Expose
    @Separator("外部のサウンドリソース")
    @Property("ガレージバンドの音源ファイル")
    @PathString(PathStringType.DIRECTORY)
    private String garageBandSoundDir;

    @Expose
    @Property("その他の音源ファイル")
    @ListProperty(String.class)
    private List<String> anotherSoundDirList;

    @Expose
    @Separator("python(未インストール時は無視されます)")
    @Property("pythonのパス(システム)")
    private String pythonPath;

    @Expose
    @Property("pythonのパス(pyenv)")
    private String pyenvPythonPath;

    @Expose
    @Property("pyenvを優先")
    private boolean forcePyenvPath;

    @Property("有効なpythonのパス(変更不可能)")
    @GetMethod
    private String activePythonPath;

    @Property("ポート番号")
    @GetMethod
    private int pythonPort;

    @Expose
    @Separator("その他")
    @SetMethod
    @GetMethod
    @Property("ルックアンドフィール")
    private String lookAndFeel;

    @Expose
    @SetMethod
    @GetMethod
    @Property("MIDIファイルタイプ")
    private int midiFileType;

    @Expose
    private PhraseList phraseList;

    @Expose
    private Map<String, SoundSampleSetting> sampleSettings;

    private PropertyChangeSupport support;
    private Optional<SoundDatabase> garagebandSoundDatabase;
    private List<Optional<SoundDatabase>> anotherSoundDatabase;

    public GlobalSetting() {
        this.support = new PropertyChangeSupport(this);
        this.beatWidth = 96;
        this.beatHeight = 24;
        this.beatSplitCount = 4;
        this.midiFileType = 1;
        this.phraseList = new PhraseList();
        this.garageBandSoundDir = "/Library/Application Support/GarageBand/Instrument Library/Sampler/Sampler Files";
        this.anotherSoundDirList = new ArrayList<>();
        this.sampleSettings = new HashMap<>();
        this.pythonPath = "";
        this.pyenvPythonPath = "";
        this.forcePyenvPath = true;
        this.pythonPort = 8180;
        PythonUtil.getSystemPythonPath().ifPresent((e) -> this.pythonPath = e);
        PythonUtil.getPyenvPythonPath().ifPresent((e) -> this.pyenvPythonPath = e);
    }

    private void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    private void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void setBeatWidth(int beatWidth) {
        int a = this.beatWidth;
        this.beatWidth = beatWidth;
        support.firePropertyChange("beatWidth", a, beatWidth);
    }

    public int getBeatWidth() {
        return beatWidth;
    }

    public void setBeatHeight(int beatHeight) {
        int a = this.beatHeight;
        this.beatHeight = beatHeight;
        support.firePropertyChange("beatHeight", a, beatHeight);
    }

    public int getBeatHeight() {
        return beatHeight;
    }

    public void setBeatSplitCount(int beatSplitCount) {
        int a = this.beatSplitCount;
        this.beatSplitCount = beatSplitCount;
        support.firePropertyChange("beatSplitCount", a, beatSplitCount);
    }

    public int getBeatSplitCount() {
        return beatSplitCount;
    }

    public void setLookAndFeel(String lookAndFeel) {
        String a = this.lookAndFeel;
        this.lookAndFeel = lookAndFeel;
        support.firePropertyChange("lookAndFeel", a, lookAndFeel);
    }

    public String getLookAndFeel() {
        return lookAndFeel;
    }

    public void setMidiFileType(int midiFileType) {
        this.midiFileType = midiFileType;
    }

    public int getMidiFileType() {
        return midiFileType;
    }

    public Optional<SoundDatabase> getGarageBandSoundDatabase() {
        if (this.garagebandSoundDatabase == null) {
            File dir = new File(this.garageBandSoundDir);
            if (dir.exists()) {
                this.garagebandSoundDatabase = Optional.of(SoundDatabase.create(dir));
            } else {
                this.garagebandSoundDatabase = Optional.empty();
            }
        }
        return garagebandSoundDatabase;
    }

    public List<Optional<SoundDatabase>> getAnotherSoundDatabases() {
        if (this.anotherSoundDatabase == null) {
            this.anotherSoundDatabase = new ArrayList<>();
            for (String dirStr : this.anotherSoundDirList) {
                File dir = new File(dirStr);
                if (dir.exists()) {
                    this.anotherSoundDatabase.add(Optional.of(SoundDatabase.create(dir)));
                } else {
                    this.anotherSoundDatabase.add(Optional.empty());
                }
            }
        }
        return anotherSoundDatabase;
    }

    public List<Optional<SoundDatabase>> getAllSoundDatabases() {
        List<Optional<SoundDatabase>> ret = new ArrayList<>();
        ret.add(getGarageBandSoundDatabase());
        ret.addAll(getAnotherSoundDatabases());
        return ret;
    }

    public PhraseList getPhraseList() {
        return phraseList;
    }

    public SoundSampleSetting getSampleSetting(String key) {
        if (!sampleSettings.containsKey(key)) {
            sampleSettings.put(key, new SoundSampleSetting());
        }
        return sampleSettings.get(key);
    }

    public String getActivePythonPath() {
        if (forcePyenvPath) {
            if (pyenvPythonPath.equals("") && !pythonPath.equals("")) {
                return pythonPath;
            }
            return pyenvPythonPath;
        }
        if (pythonPath.equals("") && !pyenvPythonPath.equals("")) {
            return pyenvPythonPath;
        }
        return pythonPath;
    }

    public int getPythonPort() {
        return pythonPort;
    }

    public static class Context {

        private static Context instance;
        private GlobalSetting setting;
        private List<PropertyChangeListener> propChangeListeners;

        private Context() {
            this.setting = new GlobalSetting();
            this.propChangeListeners = new ArrayList<>();
        }

        public static Context getInstance() {
            if (instance == null) {
                instance = new Context();
            }
            return instance;
        }

        public static GlobalSetting getGlobalSetting() {
            return getInstance().setting;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propChangeListeners.add(listener);
            setting.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propChangeListeners.remove(listener);
            setting.removePropertyChangeListener(listener);
        }

        public void save() throws IOException {
            File jsonFile = new File("GlobalSetting.json");
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(PhraseList.class, new PhraseListTypeAdapter())
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
            try ( FileWriter fw = new FileWriter(jsonFile)) {
                fw.write(gson.toJson(setting));
            }
        }

        public void load() {
            File jsonFile = new File("GlobalSetting.json");
            if (!jsonFile.exists()) {
                this.setting = new GlobalSetting();
                return;
            }
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(PhraseList.class, new PhraseListTypeAdapter())
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
            try {
                this.setting = gson.fromJson(new FileReader(jsonFile), GlobalSetting.class);
                setting.getGarageBandSoundDatabase();
                setting.getAnotherSoundDatabases();
                propChangeListeners.forEach(setting::addPropertyChangeListener);
            } catch (FileNotFoundException ex) {
                throw new Error(ex);
            }
        }
    }

}
