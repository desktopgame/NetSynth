/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.project;

import java.util.EventListener;

/**
 *
 * @author desktopgame
 */
public interface ProjectSettingListener extends EventListener {

    public void projectUpdate(ProjectSettingEvent e);
}
