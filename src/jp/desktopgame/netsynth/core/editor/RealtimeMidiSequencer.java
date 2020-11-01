/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.editor;

import jp.desktopgame.netsynth.core.project.TrackSetting;
import javax.swing.event.EventListenerList;
import jp.desktopgame.netsynth.midi.VirtualMidiEvent;
import jp.desktopgame.netsynth.midi.VirtualMidiListener;
import jp.desktopgame.netsynth.midi.VirtualMidiSequencer;
import jp.desktopgame.prc.Key;
import jp.desktopgame.prc.Note;
import jp.desktopgame.prc.NotePlayEvent;
import jp.desktopgame.prc.NotePlayEventType;
import jp.desktopgame.prc.NotePlayListener;
import jp.desktopgame.prc.PianoRollLayerUI;

/**
 *
 * @author desktopgame
 */
public class RealtimeMidiSequencer implements VirtualMidiSequencer, NotePlayListener {

    private PianoRollLayerUI layerUI;
    private TrackSetting trackSetting;
    private EventListenerList listenerList;

    public RealtimeMidiSequencer(PianoRollLayerUI layerUI, TrackSetting trackSetting) {
        this.layerUI = layerUI;
        this.trackSetting = trackSetting;
        this.listenerList = new EventListenerList();
        layerUI.addNotePlayListener(this);
    }

    @Override
    public void addVirtualMidiListener(VirtualMidiListener listener) {
        listenerList.add(VirtualMidiListener.class, listener);
    }

    @Override
    public void removeVirtualMidiListener(VirtualMidiListener listener) {
        listenerList.remove(VirtualMidiListener.class, listener);
    }

    private void fire(VirtualMidiEvent e) {
        for (VirtualMidiListener listener : listenerList.getListeners(VirtualMidiListener.class)) {
            listener.virtualPlay(e);
        }
    }

    @Override
    public void notePlay(NotePlayEvent e) {
        Note note = e.getNote();
        NotePlayEventType type = e.getType();
        Key key = note.getBeat().getMeasure().getKey();
        int height = note.getBeat().getMeasure().getKey().getModel().getKeyHeight(key.getIndex());
        if (type == NotePlayEventType.NOTE_ON) {
            fire(new VirtualMidiEvent(this, height, trackSetting.getVelocity(), true));
        } else if (type == NotePlayEventType.NOTE_OFF) {
            fire(new VirtualMidiEvent(this, height, 0, false));
        }
    }

}
