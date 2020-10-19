/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.action;

import javax.swing.AbstractAction;
import jp.desktopgame.netsynth.View;

/**
 *
 * @author desktopgame
 */
public abstract class ViewAction extends AbstractAction {

    protected View view;

    public ViewAction(View view) {
        this.view = view;
    }

}
