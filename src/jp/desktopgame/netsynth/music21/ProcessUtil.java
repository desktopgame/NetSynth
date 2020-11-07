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
 *
 * @author desktopgame
 */
public class ProcessUtil {

    private ProcessUtil() {
    }

    public static ProcessBuilder createShell(String winCmd, String unxCmd) {
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS")) {
            return new ProcessBuilder("cmd.exe", "/c", winCmd);
        } else {
            return new ProcessBuilder("/bin/bash", "-c", unxCmd);
        }
    }
}
