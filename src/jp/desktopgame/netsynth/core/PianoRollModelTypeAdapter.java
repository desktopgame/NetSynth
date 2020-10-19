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
import jp.desktopgame.prc.Beat;
import jp.desktopgame.prc.DefaultPianoRollModel;
import jp.desktopgame.prc.Key;
import jp.desktopgame.prc.Measure;
import jp.desktopgame.prc.Note;
import jp.desktopgame.prc.PianoRollModel;

/**
 *
 * @author desktopgame
 */
public class PianoRollModelTypeAdapter extends TypeAdapter<PianoRollModel> {

    @Override
    public void write(JsonWriter jw, PianoRollModel model) throws IOException {
        // {
        jw.beginObject();
        // keyCount: 36
        jw.name("keyCount").value(model.getKeyCount());
        // keys: []
        {
            jw.name("keys").beginArray();
            for (int i = 0; i < model.getKeyCount(); i++) {
                Key key = model.getKey(i);
                jw.beginObject();
                jw.name("measureCount").value(key.getMeasureCount());
                {
                    jw.name("measures").beginArray();
                    for (int j = 0; j < key.getMeasureCount(); j++) {
                        Measure measure = key.getMeasure(j);
                        jw.beginObject();
                        jw.name("beatCount").value(measure.getBeatCount());
                        jw.name("beats").beginArray();
                        for (int k = 0; k < measure.getBeatCount(); k++) {
                            Beat beat = measure.getBeat(k);
                            jw.beginObject();
                            jw.name("noteCount").value(beat.getNoteCount());
                            jw.name("notes").beginArray();
                            for (int L = 0; L < beat.getNoteCount(); L++) {
                                Note note = beat.getNote(L);
                                jw.beginObject();
                                jw.name("offset").value(note.getOffset());
                                jw.name("length").value(note.getLength());
                                jw.name("selected").value(note.isSelected());
                                jw.endObject();
                            }
                            jw.endArray();
                            jw.endObject();
                        }
                        jw.endArray();
                        jw.endObject();
                    }
                    jw.endArray();
                }
                jw.endObject();
            }
            jw.endArray();
        }
        // }
        jw.endObject();
    }

    @Override
    public PianoRollModel read(JsonReader jr) throws IOException {
        DefaultPianoRollModel model = new DefaultPianoRollModel(12 * 8, 4, 4);
        jr.beginObject();
        while (jr.hasNext()) {
            switch (jr.nextName()) {
                case "keyCount":
                    model.resizeKeyCount(jr.nextInt());
                    break;
                case "keys":
                    int keyIndex = 0;
                    jr.beginArray();
                    while (jr.hasNext()) {
                        Key key = model.getKey(keyIndex);
                        jr.beginObject();
                        while (jr.hasNext()) {
                            switch (jr.nextName()) {
                                case "measureCount":
                                    model.resizeMeasureCount(jr.nextInt());
                                    break;
                                case "measures":
                                    int measureIndex = 0;
                                    jr.beginArray();
                                    while (jr.hasNext()) {
                                        Measure measure = key.getMeasure(measureIndex);
                                        jr.beginObject();
                                        while (jr.hasNext()) {
                                            switch (jr.nextName()) {
                                                case "beatCount":
                                                    model.resizeBeatCount(jr.nextInt());
                                                    break;
                                                case "beats":
                                                    jr.beginArray();
                                                    int beatIndex = 0;
                                                    while (jr.hasNext()) {
                                                        Beat beat = measure.getBeat(beatIndex);
                                                        jr.beginObject();
                                                        while (jr.hasNext()) {
                                                            switch (jr.nextName()) {
                                                                case "noteCount":
                                                                    jr.nextInt();
                                                                    break;
                                                                case "notes":
                                                                    jr.beginArray();
                                                                    while (jr.hasNext()) {
                                                                        Note note = beat.generateNote(0, 0);
                                                                        jr.beginObject();
                                                                        while (jr.hasNext()) {
                                                                            switch (jr.nextName()) {
                                                                                case "offset":
                                                                                    note.setOffset(jr.nextInt());
                                                                                    break;
                                                                                case "length":
                                                                                    note.setLength((float) jr.nextDouble());
                                                                                    break;
                                                                                case "selected":
                                                                                    note.setSelected(jr.nextBoolean());
                                                                                    break;
                                                                            }
                                                                        }
                                                                        jr.endObject();
                                                                    }
                                                                    jr.endArray();
                                                                    break;
                                                            }
                                                        }
                                                        beatIndex++;
                                                        jr.endObject();
                                                    }
                                                    jr.endArray();
                                                    break;
                                            }
                                        }
                                        measureIndex++;
                                        jr.endObject();
                                    }
                                    jr.endArray();
                                    break;
                            }
                        }
                        keyIndex++;
                        jr.endObject();
                    }
                    jr.endArray();
                    break;
            }
        }
        jr.endObject();
        return model;
    }

}
