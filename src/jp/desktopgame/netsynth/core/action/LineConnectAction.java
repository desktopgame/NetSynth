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
import javax.sound.sampled.LineUnavailableException;
import static jp.desktopgame.netsynth.NetSynth.logException;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.GlobalSetting;
import jp.desktopgame.netsynth.core.LineConnectDialog;

/**
 *
 * @author desktopgame
 */
public class LineConnectAction extends ViewAction {

    private final Object THREAD_LOCK = new Object();

    public LineConnectAction(View view) {
        super(view);
        putValue(NAME, "ライン接続");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        startAction();
    }

    private void startAction() {
        GlobalSetting gs = GlobalSetting.Context.getGlobalSetting();
        LineConnectDialog dialog = new LineConnectDialog();
        dialog.showDialog();
        dialog.getClosedLines().stream().forEach((line) -> {
            line.close();
            try {
                line.stop();
            } catch (InterruptedException ex) {
                logException(ex);
            }
        });
        dialog.getOpenedLines().stream().forEach((line) -> {
            try {
                line.open(line.getLineAudioFormat());
                line.start();
            } catch (LineUnavailableException ex) {
                logException(ex);
            }
        });
    }
}
