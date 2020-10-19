/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.project;

import java.util.EventObject;

/**
 *
 * @author desktopgame
 */
public class ProjectSettingEvent extends EventObject {

    private ProjectSettingEventType type;
    private int index;

    public ProjectSettingEvent(ProjectSetting source, ProjectSettingEventType type, int index) {
        super(source);
        this.type = type;
        this.index = index;
    }

    @Override
    public ProjectSetting getSource() {
        return (ProjectSetting) super.getSource(); //To change body of generated methods, choose Tools | Templates.
    }

    public ProjectSettingEventType getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }

}
