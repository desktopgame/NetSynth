/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth;

import java.awt.BorderLayout;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import jp.desktopgame.netsynth.console.ConsolePane;
import jp.desktopgame.netsynth.core.action.AllLineCloseAction;
import jp.desktopgame.netsynth.core.action.AudioSliceAction;
import jp.desktopgame.netsynth.core.action.AutoRecAction;
import jp.desktopgame.netsynth.core.action.CopyAction;
import jp.desktopgame.netsynth.core.action.CopyPhraseAction;
import jp.desktopgame.netsynth.core.action.CutAction;
import jp.desktopgame.netsynth.core.action.EasyRecAction;
import jp.desktopgame.netsynth.core.action.ExportAction;
import jp.desktopgame.netsynth.core.action.GlobalSettingAction;
import jp.desktopgame.netsynth.core.action.ImportAction;
import jp.desktopgame.netsynth.core.action.KeyMapAction;
import jp.desktopgame.netsynth.core.action.LineConnectAction;
import jp.desktopgame.netsynth.core.action.LookAndFeelAction;
import jp.desktopgame.netsynth.core.action.MidiControlPanelAction;
import jp.desktopgame.netsynth.core.action.MixerControlPanelAction;
import jp.desktopgame.netsynth.core.action.NewAction;
import jp.desktopgame.netsynth.core.action.OpenAction;
import jp.desktopgame.netsynth.core.action.PasteAction;
import jp.desktopgame.netsynth.core.action.PauseAction;
import jp.desktopgame.netsynth.core.action.PlayAction;
import jp.desktopgame.netsynth.core.action.ProjectSettingAction;
import jp.desktopgame.netsynth.core.action.RedoAction;
import jp.desktopgame.netsynth.core.action.SaveAction;
import jp.desktopgame.netsynth.core.action.SaveAsAction;
import jp.desktopgame.netsynth.core.action.SavePhraseAction;
import jp.desktopgame.netsynth.core.action.SoundDatabaseControlPanelAction;
import jp.desktopgame.netsynth.core.action.StopAction;
import jp.desktopgame.netsynth.core.action.UndoAction;
import jp.desktopgame.netsynth.core.action.VersionAction;
import jp.desktopgame.netsynth.core.editor.ActionTable;
import jp.desktopgame.netsynth.core.editor.TrackChangeEvent;
import jp.desktopgame.netsynth.core.editor.TrackSetting;
import jp.desktopgame.netsynth.core.editor.WorkAreaPane;
import jp.desktopgame.netsynth.core.project.ProjectModifyEvent;
import jp.desktopgame.netsynth.core.project.ProjectSetting;
import jp.desktopgame.pec.BeanEditorPane;

/**
 *
 * @author desktopgame
 */
public class View {

    private JFrame frame;
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JPanel rootPane;
    private JSplitPane hSplit, vSplit;
    private JPanel leftPanel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private WorkAreaPane workAreaPane;
    private ConsolePane consolePane;
    private BeanEditorPane<TrackSetting> trackEditorPane;
    private ActionTable<View> actionTable;

    /* package private */ View() {
        this.frame = new JFrame();
        this.menuBar = new JMenuBar();
        this.toolBar = new JToolBar();
        this.rootPane = new JPanel(new BorderLayout());
        this.leftPanel = new JPanel(new BorderLayout());
        this.topPanel = new JPanel(new BorderLayout());
        this.bottomPanel = new JPanel(new BorderLayout());
        this.vSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        this.hSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, vSplit);
        this.workAreaPane = new WorkAreaPane();
        this.consolePane = new ConsolePane();
        this.trackEditorPane = new BeanEditorPane<>(TrackSetting.class);
        trackEditorPane.setImmediate(true);
        this.actionTable = new ActionTable(this);
        ProjectSetting.Context.getInstance().addProjectModifyListener(this::projectModified);
        workAreaPane.addTrackChangeListener(this::trackChanged);
        actionTable.register(NewAction.class);
        actionTable.register(OpenAction.class);
        actionTable.register(SaveAsAction.class);
        actionTable.register(SaveAction.class);
        actionTable.register(ImportAction.class);
        actionTable.register(ExportAction.class);
        actionTable.register(LookAndFeelAction.class);
        actionTable.register(SoundDatabaseControlPanelAction.class);
        actionTable.register(MixerControlPanelAction.class);
        actionTable.register(MidiControlPanelAction.class);
        actionTable.register(GlobalSettingAction.class);
        actionTable.register(ProjectSettingAction.class);
        actionTable.register(UndoAction.class);
        actionTable.register(RedoAction.class);
        actionTable.register(CutAction.class);
        actionTable.register(CopyAction.class);
        actionTable.register(PasteAction.class);
        actionTable.register(SavePhraseAction.class);
        actionTable.register(CopyPhraseAction.class);
        actionTable.register(PlayAction.class);
        actionTable.register(PauseAction.class);
        actionTable.register(StopAction.class);
        actionTable.register(LineConnectAction.class);
        actionTable.register(AllLineCloseAction.class);
        actionTable.register(EasyRecAction.class);
        actionTable.register(AutoRecAction.class);
        actionTable.register(KeyMapAction.class);
        actionTable.register(AudioSliceAction.class);
        actionTable.register(VersionAction.class);
        ProjectSetting.Context.getInstance().clear();
    }

    private void buildActions() {
        JMenu fileMenu = new JMenu("ファイル(F)");
        fileMenu.setMnemonic('F');
        fileMenu.add(new JMenuItem(getAction("NewAction")));
        fileMenu.add(new JMenuItem(getAction("OpenAction")));
        fileMenu.add(new JMenuItem(getAction("SaveAction")));
        fileMenu.add(new JMenuItem(getAction("SaveAsAction")));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(getAction("ImportAction")));
        fileMenu.add(new JMenuItem(getAction("ExportAction")));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(getAction("LookAndFeelAction")));
        fileMenu.add(new JMenuItem(getAction("SoundDatabaseControlPanelAction")));
        fileMenu.add(new JMenuItem(getAction("MixerControlPanelAction")));
        fileMenu.add(new JMenuItem(getAction("MidiControlPanelAction")));
        fileMenu.add(new JMenuItem(getAction("GlobalSettingAction")));
        fileMenu.add(new JMenuItem(getAction("ProjectSettingAction")));
        JMenu editMenu = new JMenu("編集(E)");
        editMenu.setMnemonic('E');
        editMenu.add(new JMenuItem(getAction("UndoAction")));
        editMenu.add(new JMenuItem(getAction("RedoAction")));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(getAction("CutAction")));
        editMenu.add(new JMenuItem(getAction("CopyAction")));
        editMenu.add(new JMenuItem(getAction("PasteAction")));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(getAction("SavePhraseAction")));
        editMenu.add(new JMenuItem(getAction("CopyPhraseAction")));
        JMenu playMenu = new JMenu("演奏(P)");
        playMenu.add(new JMenuItem(getAction("PlayAction")));
        playMenu.add(new JMenuItem(getAction("PauseAction")));
        playMenu.add(new JMenuItem(getAction("StopAction")));
        JMenu mixerMenu = new JMenu("ミキサー(M)");
        mixerMenu.add(new JMenuItem(getAction("LineConnectAction")));
        mixerMenu.add(new JMenuItem(getAction("AllLineCloseAction")));
        mixerMenu.addSeparator();
        mixerMenu.add(new JMenuItem(getAction("EasyRecAction")));
        mixerMenu.add(new JMenuItem(getAction("AutoRecAction")));
        JMenu soundMenu = new JMenu("サウンド(S)");
        soundMenu.add(new JMenuItem(getAction("KeyMapAction")));
        soundMenu.add(new JMenuItem(getAction("AudioSliceAction")));
        soundMenu.setMnemonic('S');
        JMenu helpMenu = new JMenu("ヘルプ(H)");
        helpMenu.add(new JMenuItem(getAction("VersionAction")));
        helpMenu.setMnemonic('H');

        toolBar.add(createToolButton(getAction("NewAction")));
        toolBar.add(createToolButton(getAction("OpenAction")));
        toolBar.add(createToolButton(getAction("SaveAction")));
        toolBar.addSeparator();
        toolBar.add(createToolButton(getAction("UndoAction")));
        toolBar.add(createToolButton(getAction("RedoAction")));
        toolBar.addSeparator();
        toolBar.add(createToolButton(getAction("CutAction")));
        toolBar.add(createToolButton(getAction("CopyAction")));
        toolBar.add(createToolButton(getAction("PasteAction")));
        toolBar.addSeparator();
        toolBar.add(createToolButton(getAction("PlayAction")));
        toolBar.add(createToolButton(getAction("PauseAction")));
        toolBar.add(createToolButton(getAction("StopAction")));

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(playMenu);
        menuBar.add(mixerMenu);
        menuBar.add(soundMenu);
        menuBar.add(helpMenu);
    }

    private JButton createToolButton(Action a) {
        JButton btn = new JButton(a);
        btn.setHideActionText(true);
        return btn;
    }


    /* package private */ void show() {
        buildActions();
        leftPanel.add(trackEditorPane, BorderLayout.CENTER);
        topPanel.add(workAreaPane, BorderLayout.CENTER);
        bottomPanel.add(consolePane, BorderLayout.CENTER);
        rootPane.add(toolBar, BorderLayout.NORTH);
        rootPane.add(hSplit, BorderLayout.CENTER);
        frame.setLayout(new BorderLayout());
        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(rootPane, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        SwingUtilities.invokeLater(() -> {
            hSplit.setDividerLocation(0.2);
            vSplit.setDividerLocation(0.8);
            frame.setVisible(true);
            frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
            NetSynth.logInformation("ウィンドウの初期化が完了しました。");
        });
    }

    private void projectModified(ProjectModifyEvent e) {
        frame.setTitle(e.getWindowTitle("NetSynth"));
    }

    private void trackChanged(TrackChangeEvent e) {
        int i = workAreaPane.getSelectedTrackIndex();
        if (i >= 0) {
            trackEditorPane.setTarget(workAreaPane.getSelectedTrackSetting());
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public WorkAreaPane getWorkAreaPane() {
        return workAreaPane;
    }

    public ConsolePane getConsolePane() {
        return consolePane;
    }

    public Action getAction(String key) {
        return actionTable.get(key);
    }

}
