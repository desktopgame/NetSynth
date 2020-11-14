/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import jp.desktopgame.prc.Region;
import jp.desktopgame.prc.RegionManager;

/**
 *
 * @author desktopgame
 */
public class RegionManagerTypeAdapter extends TypeAdapter<RegionManager> {

    @Override
    public void write(JsonWriter jw, RegionManager rm) throws IOException {
        jw.beginObject();
        jw.name("regions").beginArray();
        for (Region r : rm.getRegions()) {
            jw.beginObject();
            jw.name("startOffset").value(r.getStartOffset());
            jw.name("endOffset").value(r.getEndOffset());
            jw.name("loopCount").value(r.getLoopCount());
            jw.endObject();
        }
        jw.endArray();
        jw.endObject();
    }

    @Override
    public RegionManager read(JsonReader jr) throws IOException {
        RegionManager rm = new RegionManager();
        jr.beginObject();
        while (jr.hasNext()) {
            switch (jr.nextName()) {
                case "regions":
                    jr.beginArray();
                    while (jr.hasNext()) {
                        jr.beginObject();
                        int startOffset = 0, endOffset = 0, loopCount = 0;
                        while (jr.hasNext()) {
                            switch (jr.nextName()) {
                                case "startOffset":
                                    startOffset = jr.nextInt();
                                    break;
                                case "endOffset":
                                    endOffset = jr.nextInt();
                                    break;
                                case "loopCount":
                                    loopCount = jr.nextInt();
                                    break;
                            }
                        }
                        rm.addRegion(new Region(startOffset, endOffset, loopCount));
                        jr.endObject();
                    }
                    jr.endArray();
                    break;
            }
        }
        jr.endObject();
        return rm;
    }

}
