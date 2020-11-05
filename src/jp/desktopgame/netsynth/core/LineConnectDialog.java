/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jp.desktopgame.netsynth.mixer.DataLineConnection;
import jp.desktopgame.netsynth.mixer.MixerController;
import jp.desktopgame.netsynth.mixer.MixerManager;
import jp.desktopgame.pec.builder.ComboBoxHelper;
import jp.desktopgame.pec.builder.PropertyEditorBuilder;

/**
 * ライン接続ダイアログです.
 *
 * @author desktopgame
 */
public class LineConnectDialog extends JPanel {

    private ComboBoxHelper<String> lines;
    private JButton openButton, closeButton;

    private MixerManager mixerMan;
    private List<DataLineConnection> openedLineList;
    private List<DataLineConnection> closedLineList;

    public LineConnectDialog() {
        super(new BorderLayout());
        this.mixerMan = MixerManager.getInstance();
        this.openedLineList = new ArrayList<>();
        this.closedLineList = new ArrayList<>();
        // 全てのマーク状態をリセット
        mixerMan.getControllers().stream().flatMap((e) -> e.getTargetLines().stream()).forEach((e) -> e.resetMark());
    }

    public List<DataLineConnection> getOpenedLines() {
        return new ArrayList<>(openedLineList);
    }

    public List<DataLineConnection> getClosedLines() {
        return new ArrayList<>(closedLineList);
    }

    private void updateButtonState(MixerController mixerCon, int i) {
        String key = lines.at(i);
        int index = mixerCon.getTargetLineIndexFromAlias(key);
        if (index < 0) {
            openButton.setEnabled(false);
            closeButton.setEnabled(false);
            return;
        }
        DataLineConnection line = mixerCon.getTargetLines().get(index);
        openButton.setEnabled(!line.isMark());
        closeButton.setEnabled(line.isMark());
    }

    public void showDialog() {
        mixerMan.getControllers().stream().flatMap((e) -> e.getTargetLines().stream()).forEach((e) -> e.resetMark());
        PropertyEditorBuilder pb = new PropertyEditorBuilder();
        ComboBoxHelper<String> mixers = pb.comboBox("ミキサー").overwrite((Object[]) mixerMan.getControllers().stream().map((e) -> e.getMixerName()).toArray(String[]::new));
        this.lines = pb.comboBox("ターゲットライン");
        this.openButton = pb.footer(new JButton("Open"));
        this.closeButton = pb.footer(new JButton("Close"));
        openButton.setEnabled(false);
        closeButton.setEnabled(false);
        openButton.addActionListener((e) -> {
            int i = lines.index();
            if (i < 0) {
                return;
            }
            MixerController mixerCon = mixerMan.getControllers().get(mixers.index());
            DataLineConnection line = mixerCon.getTargetLines().get(mixerCon.getTargetLineIndexFromAlias(lines.at(i)));
            line.setMark(true);
            closedLineList.remove(line);
            openedLineList.add(line);
            openButton.setEnabled(false);
            closeButton.setEnabled(true);
        });
        closeButton.addActionListener((e) -> {
            int i = lines.index();
            if (i < 0) {
                return;
            }
            MixerController mixerCon = mixerMan.getControllers().get(mixers.index());
            DataLineConnection line = mixerCon.getTargetLines().get(mixerCon.getTargetLineIndexFromAlias(lines.at(i)));
            line.setMark(false);
            openedLineList.remove(line);
            closedLineList.add(line);
            openButton.setEnabled(true);
            closeButton.setEnabled(false);
        });
        mixers.onSelect((e) -> {
            int i = mixers.index();
            if (i < 0) {
                return;
            }
            MixerController mixerCon = mixerMan.getControllers().get(i);
            lines.removeAll();
            mixerCon.getTargetAliases().stream().forEach((line) -> {
                lines.add(line);
            });
            SwingUtilities.invokeLater(() -> {
                int ii = lines.index();
                if (ii >= 0) {
                    updateButtonState(mixerCon, ii);
                }
            });
        });
        lines.onSelect((e) -> {
            int i = lines.index();
            if (i >= 0) {
                MixerController mixerCon = mixerMan.getControllers().get(mixers.index());
                updateButtonState(mixerCon, i);
            }
        });
        pb.buildDialog("ライン接続").show(null);
    }

}
