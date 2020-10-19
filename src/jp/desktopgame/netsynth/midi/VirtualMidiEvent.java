/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

import java.util.EventObject;

/**
 * GUI上のシーケンサーがノートと重なったタイミングで通知されるイベントです.
 *
 * @author desktopgame
 */
public class VirtualMidiEvent extends EventObject {

    public final int height;
    public final int velocity;
    public final boolean noteOn;

    public VirtualMidiEvent(Object source, int height, int velocity, boolean noteOn) {
        super(source);
        this.height = height;
        this.velocity = velocity;
        this.noteOn = noteOn;
    }
}
