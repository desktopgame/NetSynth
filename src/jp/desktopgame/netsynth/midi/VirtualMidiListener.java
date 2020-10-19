/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

import java.util.EventListener;

/**
 * GUI上のシーケンサーと音の再生を同期するために使用されるイベントリスナーです. シーケンサーの位置がノートと重なったタイミングでコールバックされます。
 *
 * @author desktopgame
 */
public interface VirtualMidiListener extends EventListener {

    public void virtualPlay(VirtualMidiEvent e);
}
