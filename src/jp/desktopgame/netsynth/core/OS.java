/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

/**
 * OSの判別のために利用できるクラス.
 *
 * @author desktopgame
 */
public class OS {

    public static final String NAME = System.getProperty("os.name");

    private OS() {
    }

    public static boolean isMac() {
        return NAME.toLowerCase().startsWith("mac");
    }

    public static boolean isLinux() {
        return NAME.toLowerCase().startsWith("linux");
    }

    public static boolean isWindows() {
        return NAME.toLowerCase().startsWith("windows");
    }
}
