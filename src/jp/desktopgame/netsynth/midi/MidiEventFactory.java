/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;

/**
 *
 * @author desktopgame
 * @param <T>
 */
public interface MidiEventFactory<T> {

    public List<MidiEvent> create(T userObject, int channel, int timebase, double bpm, int beatWidth) throws InvalidMidiDataException;
}
