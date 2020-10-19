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
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.netsynth.resources.Resources;

/**
 *
 * @author desktopgame
 */
public class NewAction extends ViewAction {

    public NewAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "新規作成(N)");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "New16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "New24")));
        putValue(AbstractAction.MNEMONIC_KEY, (int) 'N');
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        ProjectSetting.Context.getInstance().clear();
    }

}
