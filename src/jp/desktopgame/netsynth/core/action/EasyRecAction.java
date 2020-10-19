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
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import jp.desktopgame.netsynth.NetSynth;
import static jp.desktopgame.netsynth.NetSynth.logException;
import jp.desktopgame.netsynth.View;
import jp.desktopgame.netsynth.core.action.ViewAction;
import jp.desktopgame.netsynth.mixer.DataLineConnection;

/**
 *
 * @author desktopgame
 */
public class EasyRecAction extends ViewAction {

    public EasyRecAction(View view) {
        super(view);
        putValue(NAME, "簡易録音");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        List<DataLineConnection> conns = DataLineConnection.getConnections().stream().filter((e) -> e.isOpen()).collect(Collectors.toList());
        if (conns.isEmpty()) {
            NetSynth.logInformation("一つもラインが接続されていません。");
            return;
        }
        conns.forEach(DataLineConnection::flush);
        JOptionPane.showMessageDialog(null, "このダイアログを閉じると録音が終わります。");
        for (DataLineConnection conn : conns) {
            try {
                conn.stop();
            } catch (InterruptedException ex) {
                logException(ex);
            }
        }
        int i = 0;
        for (DataLineConnection conn : conns) {
            try {
                File f = new File("easyRec_" + i + ".wav");
                conn.write(conn.getLineAudioFormat(), f);
                NetSynth.logInformation("録音終了 " + f.getPath());
                i++;
            } catch (IOException ex) {
                logException(ex);
            }
        }

    }

}
