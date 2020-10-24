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
import jp.desktopgame.netsynth.View;

/**
 *
 * @author desktopgame
 */
public class SoundAliasAction extends ViewAction {

    public SoundAliasAction(View view) {
        super(view);
        putValue(NAME, "音源の別名");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        /*
        GlobalSetting gs = GlobalSetting.Context.getGlobalSetting();
        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
        JTextField textField = new JTextField();
        List<SoundDatabase> ls = gs.getAllSoundDatabases().stream().filter((e) -> e.isPresent()).map((e) -> e.get()).collect(Collectors.toList());
        ls.stream().map((e) -> e.getDirectory().getPath()).forEach(comboBoxModel::addElement);
        JComboBox<String> comboBox = new JComboBox<>(comboBoxModel);
        comboBox.addItemListener((e) -> {
            int i = comboBox.getSelectedIndex();
            textField.setText(gs.getSoundDatabaseAlias(ls.get(i).getDirectory().getPath()));
        });
        SwingUtilities.invokeLater(() -> {
            comboBox.setSelectedIndex(0);
            textField.setText(gs.getSoundDatabaseAlias(ls.get(0).getDirectory().getPath()));
        });
        PropertyEditorDialog dialog = new PropertyEditorDialog() {
            {
                setTitle("音源の別名");
                setHiddenApplyButton(true);
                setHiddenCancelButton(true);
            }

            @Override
            protected void init() {
                super.init(); //To change body of generated methods, choose Tools | Templates.
                addLine("音源", comboBox);
                addLine("別名", textField);
                addFooter();
            }
        };
        dialog.show(null);
         */
    }

}
