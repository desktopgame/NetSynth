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
import java.util.ArrayList;
import java.util.List;
import jp.desktopgame.prc.Phrase;
import jp.desktopgame.prc.Phrase.VirtualNote;

/**
 *
 * @author desktopgame
 */
public class PhraseListTypeAdapter extends TypeAdapter<PhraseList> {

    @Override
    public void write(JsonWriter out, PhraseList value) throws IOException {
        out.beginObject();
        out.name("size").value(value.size());
        out.name("data").beginArray();
        for (Phrase p : value) {
            out.beginObject();
            List<VirtualNote> vnotes = p.getVirtualNotes();
            out.name("name").value(p.getName());
            out.name("size").value(vnotes.size());
            out.name("notes").beginArray();
            for (VirtualNote vnote : vnotes) {
                out.beginObject();
                out.name("keyIndex").value(vnote.keyIndex);
                out.name("measureIndex").value(vnote.measureIndex);
                out.name("beatIndex").value(vnote.beatIndex);
                out.name("noteOffset").value(vnote.noteOffset);
                out.name("noteLength").value(vnote.noteLength);
                out.endObject();
            }
            out.endArray();
            out.endObject();
        }
        out.endArray();
        out.endObject();
    }

    @Override
    public PhraseList read(JsonReader in) throws IOException {
        PhraseList pl = new PhraseList();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "size":
                    in.nextInt();
                    break;
                case "data":
                    in.beginArray();
                    String name = null;
                    List<VirtualNote> vnotes = new ArrayList<>();
                    while (in.hasNext()) {
                        in.beginObject();
                        while (in.hasNext()) {
                            switch (in.nextName()) {
                                case "name":
                                    name = in.nextString();
                                    break;
                                case "size":
                                    in.nextInt();
                                    break;
                                case "notes":
                                    in.beginArray();
                                    while (in.hasNext()) {
                                        in.beginObject();
                                        int ki = 0, mi = 0, bi = 0, no = 0;
                                        double nl = 0;
                                        while (in.hasNext()) {
                                            switch (in.nextName()) {
                                                case "keyIndex":
                                                    ki = in.nextInt();
                                                    break;
                                                case "measureIndex":
                                                    mi = in.nextInt();
                                                    break;
                                                case "beatIndex":
                                                    bi = in.nextInt();
                                                    break;
                                                case "noteOffset":
                                                    no = in.nextInt();
                                                    break;
                                                case "noteLength":
                                                    nl = in.nextDouble();
                                                    break;
                                            }
                                        }
                                        VirtualNote vn = new VirtualNote(ki, mi, bi, no, (float) nl);
                                        vnotes.add(vn);
                                        in.endObject();
                                    }
                                    in.endArray();
                                    break;
                            }
                        }
                        in.endObject();
                    }
                    Phrase ph = new Phrase(vnotes);
                    ph.setName(name);
                    pl.add(ph);
                    in.endArray();
                    break;
            }
        }
        in.endObject();
        return pl;
    }

}
