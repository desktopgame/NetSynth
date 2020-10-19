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
 * プロジェクトが変更されたことを通知するイベントです.
 *
 * @author desktopgame
 */
public class ProjectModifyEvent extends EventObject {

    private ProjectModifyEventType type;

    public ProjectModifyEvent(ProjectSetting source, ProjectModifyEventType type) {
        super(source);
        this.type = type;
    }

    @Override
    public ProjectSetting getSource() {
        return (ProjectSetting) super.getSource(); //To change body of generated methods, choose Tools | Templates.
    }

    public ProjectModifyEventType getType() {
        return type;
    }

    public String getWindowTitle(String a) {
        if (type == ProjectModifyEventType.MODIFY) {
            return a + " - *" + getSource().getName() + "*";
        }
        return a + " - " + getSource().getName();
    }
}
