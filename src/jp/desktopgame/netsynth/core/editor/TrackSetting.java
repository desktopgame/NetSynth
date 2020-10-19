/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.editor;

import com.google.gson.annotations.Expose;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import jp.desktopgame.pec.GetMethod;
import jp.desktopgame.pec.Property;
import jp.desktopgame.pec.SetMethod;
import jp.desktopgame.prc.PianoRollModel;

/**
 * トラックごとの設定を保存するクラスです.
 *
 * @author desktopgame
 */
public class TrackSetting {

    @Expose
    @Property("名前")
    @SetMethod
    @GetMethod
    private String name;
    @Expose
    @Property("ミュート")
    @SetMethod("setMute")
    @GetMethod
    private boolean isMute;
    @Expose
    @Property("ベロシティ")
    @SetMethod
    @GetMethod
    private int velocity;
    @Expose
    @Property("バンク")
    @SetMethod
    @GetMethod
    private int bank;
    @Expose
    @Property("プログラム")
    @SetMethod
    @GetMethod
    private int program;
    @Expose
    @Property("シンセサイザー")
    @SetMethod
    @GetMethod
    private String synthesizer;

    @Expose
    private PianoRollModel model;
    private PropertyChangeSupport support;

    public TrackSetting() {
        this.name = "Track";
        this.isMute = false;
        this.velocity = 100;
        this.bank = 0;
        this.program = 0;
        this.synthesizer = "Gervill";
        this.model = null;
        this.support = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void setName(String name) {
        String a = this.name;
        this.name = name;
        support.firePropertyChange("name", a, name);
    }

    public String getName() {
        return name;
    }

    public void setMute(boolean isMute) {
        boolean a = this.isMute;
        this.isMute = isMute;
        support.firePropertyChange("isMute", a, isMute);
    }

    public boolean isMute() {
        return isMute;
    }

    public void setVelocity(int velocity) {
        int a = this.velocity;
        this.velocity = velocity;
        support.firePropertyChange("velocity", a, velocity);
    }

    public int getVelocity() {
        return velocity;
    }

    public void setBank(int bank) {
        int a = this.bank;
        this.bank = bank;
        support.firePropertyChange("bank", a, bank);
    }

    public int getBank() {
        return bank;
    }

    public void setProgram(int program) {
        int a = this.program;
        this.program = program;
        support.firePropertyChange("program", a, program);
    }

    public int getProgram() {
        return program;
    }

    public void setSynthesizer(String synthesizer) {
        this.synthesizer = synthesizer;
    }

    public String getSynthesizer() {
        return synthesizer;
    }

    public void setModel(PianoRollModel model) {
        this.model = model;
    }

    public PianoRollModel getModel() {
        return model;
    }

    @Override
    public String toString() {
        return name;
    }

}
