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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import static jp.desktopgame.netsynth.NetSynth.logException;

/**
 *
 * @author desktopgame
 */
public class LookAndFeelDialog extends JPanel {

    private DefaultComboBoxModel<String> lafComboBoxModel;
    private JComboBox<String> lafComboBox;
    private List<UIManager.LookAndFeelInfo> lafList;

    public LookAndFeelDialog() {
        super(new BorderLayout());
        this.lafList = Arrays.asList(UIManager.getInstalledLookAndFeels());
        this.lafComboBoxModel = new DefaultComboBoxModel<>();
        for (UIManager.LookAndFeelInfo lafInfo : lafList) {
            lafComboBoxModel.addElement(lafInfo.getName());
        }
        this.lafComboBox = new JComboBox<>(lafComboBoxModel);
        JButton okButton = new JButton("OK");
        okButton.addActionListener((e) -> {
            JDialog dialog = (JDialog) SwingUtilities.getWindowAncestor((Component) e.getSource());
            dialog.dispose();
            int i = lafComboBox.getSelectedIndex();
            try {
                GlobalSetting gs = GlobalSetting.Context.getGlobalSetting();
                try {
                    gs.setLookAndFeel(lafList.get(i).getClassName());
                    GlobalSetting.Context.getInstance().save();
                } catch (IOException ex) {
                    logException(ex);
                }
                UIManager.setLookAndFeel(lafList.get(i).getClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                logException(ex);
            }
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        });
        JButton cancelButton = new JButton("取り消し");
        cancelButton.addActionListener((e) -> {
            JDialog dialog = (JDialog) SwingUtilities.getWindowAncestor((Component) e.getSource());
            dialog.dispose();
        });
        JPanel flow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        flow.add(Box.createHorizontalGlue());
        flow.add(okButton);
        flow.add(cancelButton);
        add(lafComboBox, BorderLayout.NORTH);
        add(flow, BorderLayout.SOUTH);
    }

    public void showDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("ルックアンドフィールの変更");
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.add(this);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
