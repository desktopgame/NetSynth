/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import jp.desktopgame.netsynth.console.ConsoleType;
import jp.desktopgame.netsynth.core.GlobalSetting;
import jp.desktopgame.netsynth.midi.MidiDeviceManager;
import jp.desktopgame.netsynth.mixer.MixerManager;
import jp.desktopgame.netsynth.music21.Music21;

/**
 * プログラムのエントリポイントです.
 *
 * @author desktopgame
 */
public class NetSynth {

    private static View view;
    private static Music21 music21;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GlobalSetting.Context.getInstance().load();
        MixerManager.getInstance().fetch();
        MidiDeviceManager.getInstance().fetch();
        applyLookAndFeel();
        view = new View();
        music21 = new Music21(GlobalSetting.Context.getGlobalSetting().getPythonPort());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                music21.kill();
            }
        });
        view.show();
    }

    private static void applyLookAndFeel() {
        try {
            GlobalSetting setting = GlobalSetting.Context.getGlobalSetting();
            String laf = setting.getLookAndFeel();
            if (laf == null || laf.equals("")) {
                return;
            }
            UIManager.setLookAndFeel(laf);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(NetSynth.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * メインウィンドウを返します
     *
     * @return
     */
    public static View getView() {
        return view;
    }

    /**
     * music21と通信するためのサーバを返します.
     *
     * @return
     */
    public static Music21 getMusic21() {
        return music21;
    }

    /**
     * ログウィンドウに指定の文字を出力します.
     *
     * @param ct
     * @param str
     */
    public static void log(ConsoleType ct, String str) {
        if (SwingUtilities.isEventDispatchThread()) {
            view.getConsolePane().log(ct, str);
        } else {
            SwingUtilities.invokeLater(() -> log(ct, str));
        }
    }

    /**
     * ログウィンドウにインフォメーションを出力します.
     *
     * @param str
     */
    public static void logInformation(String str) {
        log(ConsoleType.Information, str);
    }

    /**
     * ログウィンドウに警告を出力します.
     *
     * @param str
     */
    public static void logWarning(String str) {
        log(ConsoleType.Warning, str);
    }

    /**
     * ログウィンドウにエラーを出力します.
     *
     * @param str
     */
    public static void logError(String str) {
        log(ConsoleType.Error, str);
    }

    /**
     * ログウィンドウに例外情報を出力します.
     *
     * @param e
     */
    public static void logException(Exception e) {
        if (view == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            view.getConsolePane().logException(e);
        } else {
            SwingUtilities.invokeLater(() -> view.getConsolePane().logException(e));
        }
    }

    /**
     * ログウィンドウにデバッグ情報を出力します.
     *
     * @param str
     */
    public static void logDebug(String str) {
        log(ConsoleType.Debug, str);
    }

}
