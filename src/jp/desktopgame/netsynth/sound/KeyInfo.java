/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.sound;

/**
 * あるサウンドエフェクトが対応するキーの高さを表します.
 *
 * @author desktopgame
 */
public class KeyInfo {

    public final String key;
    public final int index;
    public final int category;

    public KeyInfo(String key, int index, int category) {
        this.key = key;
        this.index = index;
        this.category = category;
    }

    @Override
    public String toString() {
        return key + " " + index;
    }
}
