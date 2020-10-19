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
import java.awt.event.ItemEvent;
import java.util.Optional;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import jp.desktopgame.netsynth.sound.SoundDatabase;

/**
 *
 * @author desktopgame
 */
public class SoundDatabaseDialog extends JPanel {

    private DefaultComboBoxModel<String> comboBoxModel;
    private JComboBox<String> comboBox;
    private JPanel viewPanel;
    private DefaultTreeModel dbTreeModel;
    private JTree dbTree;
    private String title;

    public SoundDatabaseDialog() {
        this.comboBoxModel = new DefaultComboBoxModel<>();
        this.comboBox = new JComboBox<>(comboBoxModel);
        this.viewPanel = new JPanel(new BorderLayout());
        this.dbTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        this.dbTree = new JTree(dbTreeModel);
        this.title = "";
        dbTree.setCellRenderer(new SoundDatabaseTreeCellRenderer());
        dbTree.addTreeSelectionListener(this::onSelectNode);
        dbTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        viewPanel.add(new JScrollPane(dbTree), BorderLayout.WEST);
        viewPanel.add(createRightComponent(), BorderLayout.CENTER);
        comboBox.addItemListener(this::onSelect);
        GlobalSetting gs = GlobalSetting.Context.getGlobalSetting();
        addPage(gs.getGarageBandSoundDatabase());
        for (Optional<SoundDatabase> opt : gs.getAnotherSoundDatabases()) {
            addPage(opt);
        }
        setLayout(new BorderLayout());
        add(comboBox, BorderLayout.NORTH);
        add(viewPanel, BorderLayout.CENTER);
    }

    protected Component createRightComponent() {
        return new JPanel();
    }

    protected void onSelectNode(DefaultMutableTreeNode node) {

    }

    private void onSelectNode(TreeSelectionEvent e) {
        DefaultMutableTreeNode selectedNode
                = (DefaultMutableTreeNode) dbTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            return;
        }
        onSelectNode(selectedNode);
    }

    protected void onSelectSoundDatabase(SoundDatabase sdb) {

    }

    protected void onUnselectSoundDatabase() {
    }

    private void onSelect(ItemEvent e) {
        GlobalSetting gs = GlobalSetting.Context.getGlobalSetting();
        int i = comboBox.getSelectedIndex();
        Optional<SoundDatabase> sdbOpt = Optional.empty();
        if (i == 0) {
            sdbOpt = gs.getGarageBandSoundDatabase();
        } else {
            sdbOpt = gs.getAnotherSoundDatabases().get(i - 1);
        }
        if (!sdbOpt.isPresent()) {
            dbTreeModel.setRoot(new DefaultMutableTreeNode());
            onUnselectSoundDatabase();
            return;
        }
        SoundDatabase sdb = sdbOpt.get();
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(sdb);
        buildTree(node, sdb);
        dbTreeModel.setRoot(node);
        onSelectSoundDatabase(sdb);
    }

    private void buildTree(DefaultMutableTreeNode node, SoundDatabase sdb) {
        for (SoundDatabase schild : sdb.getSubDatabase()) {
            DefaultMutableTreeNode nchild = new DefaultMutableTreeNode(schild);
            node.add(nchild);
            buildTree(nchild, schild);
        }
    }

    private void addPage(Optional<SoundDatabase> sdbOpt) {
        if (!sdbOpt.isPresent()) {
            return;
        }
        SoundDatabase sdb = sdbOpt.get();
        comboBoxModel.addElement(sdb.getName());
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void showDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle(getTitle());
        dialog.setLayout(new BorderLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.add(this);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
