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
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.midi.Synthesizer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import jp.desktopgame.netsynth.core.GlobalSetting;
import jp.desktopgame.netsynth.midi.MidiDeviceController;
import jp.desktopgame.netsynth.midi.MidiDeviceManager;
import jp.desktopgame.netsynth.mixer.MixerController;
import jp.desktopgame.netsynth.mixer.MixerManager;
import jp.desktopgame.netsynth.sound.SoundDatabase;
import jp.desktopgame.pec.BeanEditorPane;
import jp.desktopgame.pec.PropertyEditorPane;

/**
 *
 * @author desktopgame
 */
public class TrackSettingPane extends JPanel {

    private BeanEditorPane<TrackSetting> trackEditorPane;
    private PropertyEditorPane trackSoundPanel;

    private DefaultComboBoxModel<String> softSynthComboBoxModel;
    private JComboBox<String> softSynthComboBox;

    private DefaultComboBoxModel<String> hardSynthComboBoxModel;
    private JComboBox<String> hardSynthComboBox;

    private DefaultComboBoxModel<String> externalSoundComboBoxModel;
    private JComboBox<String> externalSoundComboBox;

    private DefaultComboBoxModel<String> externalSoundDetailComboBoxModel;
    private JComboBox<String> externalSoundDetailComboBox;

    private DefaultComboBoxModel<String> bankComboBoxModel;
    private JComboBox<String> bankComboBox;

    private DefaultComboBoxModel<String> programComboBoxModel;
    private JComboBox<String> programComboBox;
    private List<Instrument> instList;

    private JButton soundApplyButton;
    private JButton programApplyButton;
    private Map<Integer, List<Instrument>> map;

    private List<MidiDeviceController> controllers;
    private List<MixerController> mixers;
    private List<MidiDeviceController> softSynthCons;
    private List<MidiDeviceController> hardSynthCons;
    private List<SoundDatabase> soundDatabases;
    private List<SoundDatabase> details;

    public TrackSettingPane() {
        super(new BorderLayout());
        this.trackEditorPane = new BeanEditorPane<>(TrackSetting.class);
        this.softSynthComboBoxModel = new DefaultComboBoxModel<>();
        this.softSynthComboBox = new JComboBox<>(softSynthComboBoxModel);
        this.hardSynthComboBoxModel = new DefaultComboBoxModel<>();
        this.hardSynthComboBox = new JComboBox<>(hardSynthComboBoxModel);
        this.externalSoundComboBoxModel = new DefaultComboBoxModel<>();
        this.externalSoundComboBox = new JComboBox<>(externalSoundComboBoxModel);
        this.externalSoundDetailComboBoxModel = new DefaultComboBoxModel<>();
        this.externalSoundDetailComboBox = new JComboBox<>(externalSoundDetailComboBoxModel);
        this.bankComboBoxModel = new DefaultComboBoxModel<>();
        this.bankComboBox = new JComboBox<>(bankComboBoxModel);
        this.programComboBoxModel = new DefaultComboBoxModel<>();
        this.programComboBox = new JComboBox<>(programComboBoxModel);
        this.soundApplyButton = new JButton("適用");
        this.programApplyButton = new JButton("適用");
        this.controllers = MidiDeviceManager.getInstance().getDeviceControllers();
        this.mixers = MixerManager.getInstance().getControllers();
        this.soundDatabases = GlobalSetting.Context.getGlobalSetting().getAllSoundDatabases().stream().filter((e) -> e.isPresent()).map((e) -> e.get()).collect(Collectors.toList());
        this.map = new HashMap<>();
        this.instList = new ArrayList<>();
        trackEditorPane.setImmediate(true);
        this.trackSoundPanel = new PropertyEditorPane() {
            {
                addLine(new JLabel("音源かんたん設定"));
                addLine(new JSeparator());
                addLine("ソフトシンセ", softSynthComboBox);
                addLine("ハードシンセ", hardSynthComboBox);
                addLine("外部音源", externalSoundComboBox);
                addLine("外部音源の詳細", externalSoundDetailComboBox);
                addLine(soundApplyButton);
                addLine(new JLabel("音色かんたん設定"));
                addLine(new JSeparator());
                addLine("バンク", bankComboBox);
                addLine("プログラム", programComboBox);
                addLine(programApplyButton);
                addFooter();
            }
        };
        add(trackEditorPane, BorderLayout.NORTH);
        add(trackSoundPanel, BorderLayout.CENTER);
        soundApplyButton.addActionListener(this::onSoundApply);
        programApplyButton.addActionListener(this::onColorApply);
        this.softSynthCons = controllers.stream().filter((e) -> e.isSynthesizer()).collect(Collectors.toList());
        softSynthComboBoxModel.addElement("-");
        softSynthCons.stream().map((e) -> e.getAlias()).forEach(softSynthComboBoxModel::addElement);
        this.hardSynthCons = controllers.stream().filter((e) -> !e.isSequencer()).filter((e) -> !e.isSynthesizer()).filter((e) -> e.getReceiver().isPresent()).collect(Collectors.toList());
        hardSynthComboBoxModel.addElement("-");
        hardSynthCons.stream().map((e) -> e.getAlias()).forEach(hardSynthComboBoxModel::addElement);
        externalSoundComboBoxModel.addElement("-");
        soundDatabases.stream().map((e) -> e.getName()).forEach(externalSoundComboBoxModel::addElement);
        externalSoundDetailComboBoxModel.addElement("-");
        softSynthComboBox.addItemListener(this::onSelectSoftSynth);
        hardSynthComboBox.addItemListener(this::onSelectHardSynth);
        externalSoundComboBox.addItemListener(this::onSelectExternalSound);
        externalSoundDetailComboBox.addItemListener(this::onSelectExternalSoundDetail);
        bankComboBox.addItemListener(this::onSelectBank);
    }

    private boolean chngComboBox;

    private void onSelectSoftSynth(ItemEvent e) {
        if (chngComboBox) {
            return;
        }
        chngComboBox = true;
        hardSynthComboBox.setSelectedIndex(0);
        externalSoundComboBox.setSelectedIndex(0);
        externalSoundDetailComboBox.setSelectedIndex(0);
        clearExternalSoundDetail();
        chngComboBox = false;
    }

    private void onSelectHardSynth(ItemEvent e) {
        if (chngComboBox) {
            return;
        }
        chngComboBox = true;
        softSynthComboBox.setSelectedIndex(0);
        externalSoundComboBox.setSelectedIndex(0);
        externalSoundDetailComboBox.setSelectedIndex(0);
        clearExternalSoundDetail();
        chngComboBox = false;
    }

    private void onSelectExternalSound(ItemEvent e) {
        if (chngComboBox) {
            return;
        }
        chngComboBox = true;
        clearExternalSoundDetail();
        int i = externalSoundComboBox.getSelectedIndex() - 1;
        if (i >= 0) {
            SoundDatabase sdb = this.soundDatabases.get(i);
            this.details = new ArrayList<>(sdb.getIncludedSubDatabases(true));
            details.stream().map((x) -> x.getFullName()).forEach(externalSoundDetailComboBoxModel::addElement);
        }
        softSynthComboBox.setSelectedIndex(0);
        hardSynthComboBox.setSelectedIndex(0);
        externalSoundDetailComboBox.setSelectedIndex(0);
        chngComboBox = false;
    }

    private void onSelectExternalSoundDetail(ItemEvent e) {
        if (chngComboBox) {
            return;
        }
    }

    private void onSoundApply(ActionEvent e) {
        Optional<TrackSetting> tsOpt = trackEditorPane.getTarget();
        if (!tsOpt.isPresent()) {
            return;
        }
        TrackSetting ts = tsOpt.get();
        int softI = softSynthComboBox.getSelectedIndex();
        int hardI = hardSynthComboBox.getSelectedIndex();
        int exI = externalSoundComboBox.getSelectedIndex();
        int exDI = externalSoundDetailComboBox.getSelectedIndex();
        if (softI > 0) {
            ts.setSynthesizer(softSynthComboBox.getItemAt(softI));
        } else if (hardI > 0) {
            ts.setSynthesizer(hardSynthCons.get(hardI - 1).getInfo().getName());
        } else if (exI > 0 && exDI > 0) {
            ts.setSynthesizer(externalSoundDetailComboBox.getItemAt(exDI));
        }
        trackEditorPane.loadFromInstance(ts);
        scanBank();
        //SwingUtilities.invokeLater(() -> NetSynth.getView().getFrame().revalidate());
    }

    private void onColorApply(ActionEvent e) {
        int bank = Integer.valueOf((String) bankComboBox.getSelectedItem());
        int program = programComboBox.getSelectedIndex();
        if (program <= 0) {
            return;
        }
        Optional<TrackSetting> tsOpt = getTarget();
        if (!tsOpt.isPresent()) {
            return;
        }
        Patch patch = instList.get(program - 1).getPatch();
        TrackSetting ts = tsOpt.get();
        ts.setBank(bank);
        ts.setProgram(patch.getProgram());
        trackEditorPane.loadFromInstance(ts);
    }

    private void onSelectBank(ItemEvent e) {
        int i = bankComboBox.getSelectedIndex();
        if (i <= 0) {
            return;
        }
        int ss = softSynthComboBox.getSelectedIndex();
        if (ss <= 0) {
            return;
        }
        MidiDeviceController con = controllers.get(ss - 1);
        if (!con.isSynthesizer()) {
            return;
        }
        Synthesizer synthesizer = con.getSynthesizer().get();
        programComboBoxModel.removeAllElements();
        programComboBoxModel.addElement("-");
        instList.clear();
        int s = Integer.valueOf(bankComboBoxModel.getElementAt(i));
        for (Instrument inst : synthesizer.getAvailableInstruments()) {
            Patch p = inst.getPatch();
            int bank = p.getBank();
            if (bank != s) {
                continue;
            }
            int program = p.getProgram();
            instList.add(inst);
            programComboBoxModel.addElement(String.valueOf(program) + " - " + inst.toString());
        }
    }

    private void clearExternalSoundDetail() {
        externalSoundDetailComboBoxModel.removeAllElements();
        externalSoundDetailComboBoxModel.addElement("-");
    }

    private void scanBank() {
        MidiDeviceController con = controllers.get(softSynthComboBox.getSelectedIndex());
        if (!con.isSynthesizer()) {
            return;
        }
        Synthesizer synthesizer = con.getSynthesizer().get();
        scanBank(synthesizer);
    }

    private void scanBank(Synthesizer synthesizer) {
        List<Integer> banks = new ArrayList<>();
        bankComboBoxModel.removeAllElements();
        bankComboBoxModel.addElement("-");
        programComboBoxModel.removeAllElements();
        programComboBoxModel.addElement("-");
        for (Instrument inst : synthesizer.getAvailableInstruments()) {
            Patch p = inst.getPatch();
            int bank = p.getBank();
            if (!banks.contains(bank)) {
                banks.add(bank);
                bankComboBoxModel.addElement(String.valueOf(bank));
            }
        }
    }

    public void setTarget(TrackSetting ts) {
        trackEditorPane.setTarget(ts);
        Optional<MidiDeviceController> midiCon = MidiDeviceManager.getInstance()
                .getDeviceControllers()
                .stream()
                .filter((e) -> e.getInfo().getName().equals(ts.getSynthesizer()))
                .findFirst();
        if (midiCon.isPresent()) {
            int i = softSynthCons.stream().map((e) -> e.getAlias()).collect(Collectors.toList()).indexOf(ts.getSynthesizer());
            if (i >= 0) {
                softSynthComboBox.setSelectedIndex(i + 1);
            }
            scanBank(midiCon.get().getSynthesizer().get());
        }
    }

    public Optional<TrackSetting> getTarget() {
        return trackEditorPane.getTarget();
    }
}
