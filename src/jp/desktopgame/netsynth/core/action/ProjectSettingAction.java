/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import static jp.desktopgame.netsynth.NetSynth.logException;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.pec.BeanEditorDialog;

/**
 *
 * @author desktopgame
 */
public class ProjectSettingAction extends ViewAction {

    private boolean modified;

    public ProjectSettingAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "プロジェクト設定");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (!ProjectSetting.Context.getInstance().getFilePath().isPresent()) {
            view.getAction("SaveAsAction").actionPerformed(arg0);
            return;
        }
        BeanEditorDialog<ProjectSetting> d = new BeanEditorDialog<ProjectSetting>(ProjectSetting.class, (ProjectSetting ps) -> {
            try {
                ProjectSetting.Context.getInstance().save();
            } catch (IOException ex) {
                logException(ex);
            }
        });
        d.setTarget(ProjectSetting.Context.getProjectSetting());
        d.show(null, modified);
        this.modified = d.isModified();
    }

}
