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
import jp.desktopgame.netsynth.core.GlobalSetting;
import jp.desktopgame.pec.BeanEditorDialog;

/**
 *
 * @author desktopgame
 */
public class GlobalSettingAction extends ViewAction {

    private boolean modified;

    public GlobalSettingAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "グローバル設定");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        BeanEditorDialog<GlobalSetting> d = new BeanEditorDialog<GlobalSetting>(GlobalSetting.class, (GlobalSetting gs) -> {
            try {
                GlobalSetting.Context.getInstance().save();
            } catch (IOException ex) {
                logException(ex);
            }
        });
        d.setTarget(GlobalSetting.Context.getGlobalSetting());
        d.show(null, modified);
        this.modified = d.isModified();
    }

}
