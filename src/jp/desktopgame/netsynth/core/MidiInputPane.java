/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import com.google.gson.Gson;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import jp.desktopgame.netsynth.NetSynth;
import static jp.desktopgame.netsynth.NetSynth.logInformation;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.netsynth.core.project.TrackSetting;
import jp.desktopgame.netsynth.midi.MidiDeviceController;
import jp.desktopgame.netsynth.midi.MidiDeviceManager;
import jp.desktopgame.netsynth.music21.JsonResponse;
import jp.desktopgame.pec.PropertyEditorPane;
import jp.desktopgame.sbc.SlotCallback;

/**
 *
 * @author desktopgame
 */
public class MidiInputPane extends JPanel implements SlotCallback {

    private List<MidiDeviceController> controllers;
    private DefaultComboBoxModel<String> keyboardComboBoxModel;
    private JComboBox<String> keyboardComboBox;

    private DefaultComboBoxModel<String> trackComboBoxModel;
    private JComboBox<String> trackComboBox;

    private JCheckBox enabledCheckBox;
    private Map<Transmitter, MyReceiver> trMap;
    private static Timer swingTimer, chordTimer;

    static {
        swingTimer = new Timer(0, (e) -> {
        });
        swingTimer.setDelay(1000);
        swingTimer.setRepeats(true);
        chordTimer = new Timer(0, (e) -> {
        });
        chordTimer.setDelay(100);
        chordTimer.setRepeats(false);
    }

    public MidiInputPane() {
        super(new BorderLayout());
        this.controllers = new ArrayList<>();
        this.keyboardComboBox = new JComboBox<>(this.keyboardComboBoxModel = new DefaultComboBoxModel<>());
        this.trackComboBox = new JComboBox<>(this.trackComboBoxModel = new DefaultComboBoxModel<>());
        this.enabledCheckBox = new JCheckBox();
        this.trMap = new HashMap<>();
        PropertyEditorPane editor = new PropertyEditorPane() {
            {
                addLine(new JLabel("任意のMIDIキーボードの入力をリアルタイムでトラックへ送信します。"));
                addLine("MIDIキーボード", keyboardComboBox);
                addLine("対象トラック", trackComboBox);
                addLine("有効", enabledCheckBox);
                addFooter();
            }
        };
        keyboardComboBox.addItemListener(this::onSelectKeyboard);
        trackComboBox.addItemListener(this::onSelectTrack);
        enabledCheckBox.addActionListener(this::onEnabled);
        add(editor, BorderLayout.CENTER);
    }

    private void onSelectKeyboard(ItemEvent e) {
        int keyboard = keyboardComboBox.getSelectedIndex();
        if (keyboard < 0) {
            return;
        }
        Transmitter t = controllers.get(keyboard).getSharedTransmitter(0).get();
        enabledCheckBox.setSelected(false);
        if (!trMap.containsKey(t)) {
            return;
        }
        NetSynth.getView().getWorkAreaPane().setupMidiPlayer();
        enabledCheckBox.setSelected(trMap.get(t).enabled);
        ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
        ps.getTrackSetting(trMap.get(t).uuid).ifPresent((track) -> {
            for (int i = 0; i < trackComboBox.getItemCount(); i++) {
                if (ps.getTrackSetting(i).getUUID().equals(track.getUUID())) {
                    trackComboBox.setSelectedIndex(i);
                    break;
                }
            }
        });
    }

    private void onSelectTrack(ItemEvent e) {
        getReceiver().ifPresent((r) -> {
            NetSynth.getView().getWorkAreaPane().setupMidiPlayer();
            ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
            r.uuid = ps.getTrackSetting(trackComboBox.getSelectedIndex()).getUUID();
            r.enabled = enabledCheckBox.isSelected();
            r.logInfo();
        });
    }

    private void onEnabled(ActionEvent e) {
        getReceiver().ifPresent((r) -> {
            NetSynth.getView().getWorkAreaPane().setupMidiPlayer();
            r.enabled = enabledCheckBox.isSelected();
            r.logInfo();
        });
    }

    private Optional<MyReceiver> getReceiver() {
        int i = keyboardComboBox.getSelectedIndex();
        if (i < 0) {
            return Optional.empty();
        }
        int track = trackComboBox.getSelectedIndex();
        if (track < 0) {
            return Optional.empty();
        }
        ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
        Transmitter t = controllers.get(i).getSharedTransmitter(0).get();
        if (!trMap.containsKey(t)) {
            MyReceiver mr = new MyReceiver(ps.getTrackSetting(track).getUUID(), t.getReceiver());
            trMap.put(t, mr);
            t.setReceiver(mr);
        }
        return Optional.of(trMap.get(t));
    }

    @Override
    public void onShow() {
        NetSynth.getView().getWorkAreaPane().setupMidiPlayer();
        keyboardComboBoxModel.removeAllElements();
        trackComboBoxModel.removeAllElements();
        enabledCheckBox.setSelected(false);
        this.controllers = MidiDeviceManager.getInstance().getDeviceControllers().stream().filter((e) -> e.getMaxTransmitters() > 0 || e.getMaxTransmitters() == -1).collect(Collectors.toList());
        ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
        controllers.stream().map((e) -> e.getAlias()).forEach(keyboardComboBoxModel::addElement);
        for (int i = 0; i < ps.getGUITrackSettingCount(); i++) {
            trackComboBoxModel.addElement(ps.getTrackSetting(i).getName());
        }
    }

    @Override
    public void onHide() {
    }

    private class MyReceiver implements Receiver {

        private Receiver receiver;
        public String uuid;
        public boolean enabled;

        private List<Integer> keys;

        public MyReceiver(String uuid, Receiver receiver) {
            this.uuid = uuid;
            this.receiver = receiver;
            this.enabled = true;
            this.keys = new ArrayList<>();
            ProjectSetting ps = ProjectSetting.Context.getProjectSetting();
            swingTimer.addActionListener((e) -> {
                boolean found = false;
                for (int i = 0; i < ps.getGUITrackSettingCount(); i++) {
                    TrackSetting g = ps.getTrackSetting(i);
                    if (g.getUUID().equals(uuid)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    enabled = false;
                    logInfo();
                }
            });
            chordTimer.addActionListener((e) -> {
                List<String> notes = keys.stream().map((k) -> {
                    int oct = k / 12;
                    int index = k % 12;
                    StringBuilder sb = new StringBuilder();
                    switch (index) {
                        case 0:
                            sb.append("C");
                            break;
                        case 1:
                            sb.append("C#");
                            break;
                        case 2:
                            sb.append("D");
                            break;
                        case 3:
                            sb.append("D#");
                            break;
                        case 4:
                            sb.append("E");
                            break;
                        case 5:
                            sb.append("F");
                            break;
                        case 6:
                            sb.append("F#");
                            break;
                        case 7:
                            sb.append("G");
                            break;
                        case 8:
                            sb.append("G#");
                            break;
                        case 9:
                            sb.append("A");
                            break;
                        case 10:
                            sb.append("A#");
                            break;
                        case 11:
                            sb.append("B");
                            break;
                    }
                    if (oct > 0) {
                        sb.append(String.valueOf(oct));
                    }
                    return sb.toString();
                }).collect(Collectors.toList());
                execShowChord(notes);
                keys.clear();
            });
        }

        private void execShowChord(List<String> notes) {
            if (!NetSynth.getMusic21().isActive()) {
                logInformation("コードネームを表示できません。pythonパスを設定してください。");
                return;
            }
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return NetSynth.getMusic21().chordName(notes.toArray(new String[notes.size()])).get();
                }

                @Override
                protected void done() {
                    super.done(); //To change body of generated methods, choose Tools | Templates.
                    try {
                        String resp = get();
                        if (resp.equals("")) {
                            return;
                        }
                        JsonResponse jresp = new Gson().fromJson(resp, JsonResponse.class);
                        if (jresp.status != 0) {
                            return;
                        }
                        ChordName name = new Gson().fromJson(resp, ChordName.class);
                        if (name.status == 1) {
                            return;
                        }
                        NetSynth.getView().getWorkAreaPane().showChordLabel(name.value);
                    } catch (InterruptedException | ExecutionException ex) {
                        NetSynth.logException(ex);
                    }
                }

            }.execute();
        }

        public void logInfo() {
            Optional<TrackSetting> tsOpt = ProjectSetting.Context.getProjectSetting().getTrackSetting(uuid);
            if (!tsOpt.isPresent()) {
                return;
            }
            TrackSetting ts = tsOpt.get();
            if (this.enabled) {
                NetSynth.logInformation(String.format("接続されました: %s", ts.getName()));
            } else {
                NetSynth.logInformation(String.format("接続が解除されました: %s", ts.getName()));
            }
        }

        @Override
        public void send(MidiMessage arg0, long arg1) {
            if (receiver != null) {
                receiver.send(arg0, arg1);
            }
            if (!(arg0 instanceof ShortMessage)) {
                return;
            }
            Optional<TrackSetting> tsOpt = ProjectSetting.Context.getProjectSetting().getTrackSetting(uuid);
            if (!tsOpt.isPresent()) {
                return;
            }
            TrackSetting ts = tsOpt.get();
            if (!enabled || ts.isMute()) {
                return;
            }
            ShortMessage sm = (ShortMessage) arg0;
            int height = sm.getData1();
            int velocity = sm.getData2();
            int track = NetSynth.getView().getWorkAreaPane().getSelectedTrackIndex();
            if (sm.getCommand() == ShortMessage.NOTE_ON) {
                NetSynth.getView().getWorkAreaPane().noteOn(ts, height, velocity);
            } else if (sm.getCommand() == ShortMessage.NOTE_OFF) {
                NetSynth.getView().getWorkAreaPane().noteOff(ts, height, velocity);
            }
            if (track >= 0) {
                NetSynth.getView().getWorkAreaPane().getEditor(track).getKeyboard().setHighlight(height, sm.getCommand() == ShortMessage.NOTE_ON);
            }
            keys.add(height);
            chordTimer.restart();
        }

        @Override
        public void close() {
            if (receiver != null) {
                receiver.close();
            }
        }

    }
}
