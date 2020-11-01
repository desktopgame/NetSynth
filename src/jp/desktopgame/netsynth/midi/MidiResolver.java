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
import javax.sound.midi.MidiChannel;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import jp.desktopgame.netsynth.sound.SoundDatabase;

/**
 * Midiの依存関係設定から適切な{@link jp.desktopgame.netsynth.midi.MidiPlayer}を解決するクラスです.
 *
 * @author desktopgame
 */
public class MidiResolver {

    private List<MidiPlayerDependency> settings;

    public MidiResolver(List<MidiPlayerDependency> settings) {
        this.settings = new ArrayList<>(settings);
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
            List<MidiPlayerDependency> otherSettings = settings.stream().filter((e) -> e.synthesizer.equals(k)).collect(Collectors.toList());
            Synthesizer synthesizer = controllerOpt.get().getSynthesizer().get();
            // 全てにチャンネルを割り当てる
            boolean usedDrum = false;
            for (int i = 0; i < otherSettings.size(); i++) {
                int channelIndex = i;
                MidiPlayerDependency os = otherSettings.get(i);
                if (usedDrum && channelIndex >= 9) {
                    channelIndex++;
                }
                if (os.setting.isDrum && !os.setting.isMute) {
                    channelIndex = 9;
                    usedDrum = true;
                }
                MidiChannel channel = synthesizer.getChannels()[channelIndex];
                MidiPlayer player = new MidiChannelPlayer(channel);
                os.player = Optional.of(player);
                player.setup(os.setting);
                players.add(player);
            }
            synthesizerMap.put(k, Optional.empty());
        });

        // 他のトラックと重複しないシンセサイザーを使用するトラックのリスト
        List<MidiPlayerDependency> rawSettings = settings.stream().filter(((e) -> !synthesizerMap.containsKey(e.synthesizer))).collect(Collectors.toList());
        // MidiDirectPlayerを使ってイベントを送信
        for (int i = 0; i < rawSettings.size(); i++) {
            final MidiPlayerDependency setting = rawSettings.get(i);
            Optional<MidiDeviceController> midiConOpt = controllers.stream().filter((e) -> e.getReceiver().isPresent()).filter((e) -> e.getInfo().getName().equals(setting.synthesizer)).findFirst();
            if (!midiConOpt.isPresent()) {
                final int index = i;
                Optional<SoundDatabase> sdbOpt = SoundDatabase.get(setting.synthesizer);
                sdbOpt.ifPresent((sdb) -> {
                    MidiPlayer player = new MidiSEPlayer(sdb);
                    setting.player = Optional.of(player);
                    player.setup(setting.setting);
                    players.add(player);
                });
                continue;
            }
            MidiDeviceController midiCon = midiConOpt.get();
            // シンセサイザーならチャンネルを使用する
            if (!synthesizerMap.containsKey(setting.synthesizer) && midiCon.isSynthesizer()) {
                Synthesizer synthesizer = midiCon.getSynthesizer().get();
                MidiChannel channel = synthesizer.getChannels()[setting.setting.isDrum ? 9 : 0];
                MidiPlayer player = new MidiChannelPlayer(channel);
                setting.player = Optional.of(player);
                player.setup(setting.setting);
                players.add(player);
            } else {
                // そうでなければレシーバに送信する
                MidiPlayer player = new MidiReceiverPlayer(midiCon.getReceiver().get());
                player.setup(setting.setting);
                players.add(player);
            }
        }
        return players;
    }

    private Map<String, Integer> settingsToMap(List<MidiPlayerDependency> settings) {
        Map<String, Integer> r = new HashMap<>();
        for (MidiPlayerDependency s : settings) {
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
