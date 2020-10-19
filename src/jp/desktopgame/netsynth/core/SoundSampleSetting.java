/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import com.google.gson.annotations.Expose;

/**
 *
 * @author desktopgame
 */
public class SoundSampleSetting {

    @Expose
    public float peakStart;

    @Expose
    public float peakEnd;

    @Expose
    public int keyHeight;

    public SoundSampleSetting() {
        this.peakStart = 0;
        this.peakEnd = 1;
        this.keyHeight = -1;
    }

}
