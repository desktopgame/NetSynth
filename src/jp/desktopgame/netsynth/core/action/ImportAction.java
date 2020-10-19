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
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.resources.Resources;

/**
 *
 * @author desktopgame
 */
public class ImportAction extends ViewAction {

    public ImportAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "インポート");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Import16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "Import24")));
        setEnabled(false);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
    }
}
