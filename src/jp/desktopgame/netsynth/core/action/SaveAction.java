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
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import static jp.desktopgame.netsynth.NetSynth.logException;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.netsynth.resources.Resources;

/**
 *
 * @author desktopgame
 */
public class SaveAction extends ViewAction {

    public SaveAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "保存(S)");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Save16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Save24")));
        putValue(AbstractAction.MNEMONIC_KEY, (int) 'S');
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        ProjectSetting.Context pctx = ProjectSetting.Context.getInstance();
        if (pctx.getFilePath().isPresent()) {
            try {
                ProjectSetting.Context.getInstance().save();
            } catch (IOException ex) {
                logException(ex);
            }
        } else {
            view.getAction("SaveAsAction").actionPerformed(arg0);
        }
    }

}
