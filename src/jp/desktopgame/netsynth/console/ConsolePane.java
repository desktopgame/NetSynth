/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.console;

import java.awt.BorderLayout;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 * ユーザに対するメッセージを表示するための領域です.
 *
 * @author desktopgame
 */
public class ConsolePane extends JPanel {

    private JTabbedPane tabbedPane;
    private Map<ConsoleType, JTextArea> textAreaMap;

    public ConsolePane() {
        super(new BorderLayout());
        this.tabbedPane = new JTabbedPane();
        this.textAreaMap = new HashMap<>();
        for (ConsoleType ct : ConsoleType.values()) {
            JTextArea jt = new JTextArea();
            jt.setEditable(false);
            tabbedPane.addTab(ct.toString(), new JScrollPane(jt));
            textAreaMap.put(ct, jt);
        }
        add(tabbedPane, BorderLayout.CENTER);
    }

    public void log(ConsoleType ct, String str) {
        tabbedPane.setSelectedIndex(ct.ordinal());
        getTextArea(ct).append(str);
        getTextArea(ct).append(System.lineSeparator());
    }

    public void logInformation(String str) {
        log(ConsoleType.Information, str);
    }

    public void logWarning(String str) {
        log(ConsoleType.Warning, str);
    }

    public void logError(String str) {
        log(ConsoleType.Error, str);
    }

    public void logException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        log(ConsoleType.Exception, sw.toString());
    }

    public void logDebug(String str) {
        log(ConsoleType.Debug, str);
    }

    public JTextArea getTextArea(ConsoleType ct) {
        return textAreaMap.get(ct);
    }
}
