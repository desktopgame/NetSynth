/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.midi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

/**
 * Midiの依存関係設定から適切な{@link jp.desktopgame.netsynth.midi.MidiPlayer}を解決するクラスです.
 *
 * @author desktopgame
 * @param <T>
 */
public class MidiResolver<T> {

    private MidiEventFactory<T> eventFactory;
    private int timebase;
    private int bpm;
    private List<MidiPlayerDependency<T>> settings;
    private int beatWidth;

    public MidiResolver(MidiEventFactory<T> eventFactory, List<MidiPlayerDependency<T>> settings, int timebase, int bpm, int beatWidth) {
        this.settings = new ArrayList<>(settings);
        this.eventFactory = eventFactory;
        this.timebase = timebase;
        this.bpm = bpm;
        this.beatWidth = beatWidth;
    }

    /**
     * Midiプレイヤーの一覧を返します.
     *
     * @return
     */
    public List<MidiPlayer> resolve() {
        List<MidiPlayer> players = new ArrayList<>();
        List<MidiDeviceController> controllers = MidiDeviceManager.getInstance().getDeviceControllers();
        // MIDIレシーバ名/再生トラック数のペアを作成
        Map<String, Integer> settingMap = settingsToMap(settings);
        Map<String, Optional<MidiDeviceController>> controllerMap = new HashMap<>();
        Map<String, Optional<Sequencer>> synthesizerMap = new HashMap<>();
        // シンセサイザー名/シンセサイザーのペアを作成
        // ただし、複数のトラックから参照されているシンセサイザー名のみを対象とする
        settingMap.forEach((String k, Integer v) -> {
            if (v == 1) {
                return;
            }
            controllerMap.put(k,
                    controllers
                            .stream()
                            .filter(((e) -> e.getInfo().getName().equals(k)))
                            .filter(((e) -> e.isSynthesizer()))
                            .findFirst());
        });
        // シンセサイザーからチャンネルを取得して、トラックそれぞれに割り当てる
        controllerMap.forEach((String k, Optional<MidiDeviceController> controllerOpt) -> {
            // シンセサイザー名が間違っていて、取得できなかった
            if (!controllerOpt.isPresent()) {
                synthesizerMap.put(k, Optional.empty());
                return;
            }
            // 同じシンセサイザーを参照する他のトラックを取得
            List<MidiPlayerDependency<T>> otherSettings = settings.stream().filter((e) -> e.synthesizer.equals(k)).collect(Collectors.toList());
            Synthesizer synthesizer = controllerOpt.get().getSynthesizer().get();
            // 全てにチャンネルを割り当てる
            for (int i = 0; i < otherSettings.size(); i++) {
                MidiPlayerDependency<T> os = otherSettings.get(i);
                MidiChannel channel = synthesizer.getChannels()[i];
                MidiPlayer player = new MidiChannelPlayer(channel);
                try {
                    player.setup(os.setting, eventFactory.create(os.userObject, i, timebase, beatWidth, bpm), timebase, bpm);
                    players.add(player);
                } catch (InvalidMidiDataException ex) {
                }
            }
            synthesizerMap.put(k, Optional.empty());
        });

        // 他のトラックと重複しないシンセサイザーを使用するトラックのリスト
        List<MidiPlayerDependency<T>> rawSettings = settings.stream().filter(((e) -> !synthesizerMap.containsKey(e.synthesizer))).collect(Collectors.toList());
        // MidiDirectPlayerを使ってイベントを送信
        for (int i = 0; i < rawSettings.size(); i++) {
            final MidiPlayerDependency<T> setting = rawSettings.get(i);
            Optional<MidiDeviceController> midiConOpt = controllers.stream().filter((e) -> e.getInfo().getName().equals(setting.synthesizer)).findFirst();
            if (!midiConOpt.isPresent()) {
                continue;
            }
            MidiPlayer player = new MidiReceiverPlayer(midiConOpt.get().getReceiver().get());
            try {
                player.setup(setting.setting, eventFactory.create(setting.userObject, i, timebase, beatWidth, bpm), timebase, bpm);
                players.add(player);
            } catch (InvalidMidiDataException ex) {
            }
        }
        return players;
    }

    private Map<String, Integer> settingsToMap(List<MidiPlayerDependency<T>> settings) {
        Map<String, Integer> r = new HashMap<>();
        for (MidiPlayerDependency<T> s : settings) {
            if (!r.containsKey(s.synthesizer)) {
                r.put(s.synthesizer, 1);
            } else {
                int i = r.get(s.synthesizer);
                r.put(s.synthesizer, i + 1);
            }
        }
        return r;
    }

}
