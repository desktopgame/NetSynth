/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.core.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import static jp.desktopgame.netsynth.NetSynth.logException;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.netsynth.resources.Resources;

/**
 *
 * @author desktopgame
 */
public class SaveAsAction extends ViewAction {

    public SaveAsAction(View view) {
        super(view);
        putValue(AbstractAction.NAME, "名前をつけて保存(A)");
        putValue(AbstractAction.SMALL_ICON, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "SaveAs16")));
        putValue(AbstractAction.LARGE_ICON_KEY, new ImageIcon(Resources.getResourceLocation(Resources.Category.General, "SaveAs24")));
        putValue(AbstractAction.MNEMONIC_KEY, (int) 'A');
        putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control A"));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        ProjectSetting.Context pctx = ProjectSetting.Context.getInstance();
        String name = JOptionPane.showInputDialog("プロジェクト名を入力してください");
        if (name == null || name.equals("")) {
            return;
        }
        pctx.getSetting().setName(name);
        // 保存先を決定
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith(".json");
            }

            @Override
            public String getDescription() {
                return "*.json";
            }
        });
        fc.setSelectedFile(new File(name + ".json"));
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                ProjectSetting.Context.getInstance().saveAs(file.getPath());
            } catch (FileNotFoundException ex) {
                logException(ex);
            } catch (IOException ex) {
                logException(ex);
            }
        }
    }

}
