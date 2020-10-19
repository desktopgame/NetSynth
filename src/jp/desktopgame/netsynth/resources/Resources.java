/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.resources;

import java.net.URL;

/**
 *
 * @author desktopgame
 */
public class Resources {

    public enum Category {
        Development,
        General,
        Media,
        Navigation,
        Table,
        Text
    }

    private Resources() {
    }

    public static URL getResourceLocation(Category category, String name) {
        return Resources.class.getResource(category.toString().toLowerCase() + "/" + name + ".gif");
    }
}
