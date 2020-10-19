/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.editor;

import java.util.EventObject;

/**
 *
 * @author desktopgame
 */
public class TrackChangeEvent extends EventObject {

    public TrackChangeEvent(WorkAreaPane e) {
        super(e);
    }
}
