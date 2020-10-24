/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 * プログラムのエントリポイントです.
 *
 * @author desktopgame
 */
public class NetSynth {

    private static View view;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GlobalSetting.Context.getInstance().load();
        MixerManager.getInstance().fetch();
        MidiDeviceManager.getInstance().fetch();
        applyLookAndFeel();
        view = new View();
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

    public static View getView() {
        return view;
    }

    public static void log(ConsoleType ct, String str) {
        if (SwingUtilities.isEventDispatchThread()) {
            view.getConsolePane().log(ct, str);
        } else {
            SwingUtilities.invokeLater(() -> log(ct, str));
        }
    }

    public static void logInformation(String str) {
        log(ConsoleType.Information, str);
    }

    public static void logWarning(String str) {
        log(ConsoleType.Warning, str);
    }

    public static void logError(String str) {
        log(ConsoleType.Error, str);
    }

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

    public static void logDebug(String str) {
        log(ConsoleType.Debug, str);
    }

}
