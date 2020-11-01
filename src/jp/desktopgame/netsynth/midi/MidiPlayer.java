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
import javax.sound.midi.MidiEvent;

/**
 * MIDIを使用して音を鳴らす手段を共通化します.
 * ある実装ではシーケンサーが利用され、また別の手段ではレシーバにMIDIイベントを送るなどの方法を用います。
 *
 * @author desktopgame
 */
public interface MidiPlayer {

    /**
     * プレイヤーを初期化します. 同期設定がONの場合、このメソッドを呼び出した時点で同期を開始します。
     *
     * @param setting
     * @param events
     * @param timebase
     * @param bpm
     */
    public void setup(MidiPlayerSetting setting, List<MidiEvent> events, int timebase, int bpm);

    /**
     * プレイヤー初期化時の設定情報を破棄します.
     */
    public void reset();

    /**
     * 音色を変更します.
     *
     * @param bank
     * @param program
     */
    public void programChange(int bank, int program);

    /**
     * ミュート状態を変更します.
     *
     * @param isMute
     */
    public void mute(boolean isMute);

    /**
     * 現在のステータスを返します.
     *
     * @return
     */
    public MidiPlayerStatus getStatus();
}
