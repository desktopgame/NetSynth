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
 * プロジェクトの変更を監視するイベントリスナーです.
 *
 * @author desktopgame
 */
public interface ProjectModifyListener extends EventListener {

    public void projectModified(ProjectModifyEvent e);
}
