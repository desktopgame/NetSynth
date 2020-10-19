/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

/**
 * GUI上でシーケンサーをエミュレートする何らかのグラフィックが実装するインターフェイスです.
 *
 * @author desktopgame
 */
public interface VirtualMidiSequencer {

    public void addVirtualMidiListener(VirtualMidiListener listener);

    public void removeVirtualMidiListener(VirtualMidiListener listener);
}
