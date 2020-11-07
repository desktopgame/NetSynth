/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.music21;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import static jp.desktopgame.netsynth.NetSynth.logException;
import static jp.desktopgame.netsynth.NetSynth.logInformation;
import jp.desktopgame.netsynth.core.GlobalSetting;

/**
 * music21serverの機能を使用するためのヘルパークラスです.
 *
 * @author desktopgame
 */
public class Music21 {

    private Process process;
    private int port;

    public Music21(int port) {
        List<String> argv = new ArrayList<String>();
        try {
            this.port = port;
            if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS")) {
                argv.add("cmd.exe");
                argv.add("/c");
            } else {
                argv.add("/bin/bash");
                argv.add("-c");
            }
            String cmd = String.format("%s -m %s %d", GlobalSetting.Context.getGlobalSetting().getActivePythonPath(), "music21server.main", port);
            argv.add(cmd);
            logInformation(String.format("%s", cmd));
            this.process = new ProcessBuilder(argv).start();
        } catch (IOException ex) {
            logException(ex);
            this.process = null;
        }
    }

    /**
     * サーバーがアクティブであるならtrueを返します.
     *
     * @return
     */
    public boolean isActive() {
        return process != null;
    }

    /**
     * 指定の音で構成されるコードのコード名を返します.
     *
     * @param notes
     * @return
     */
    public Future<String> chordName(String... notes) {
        if (process == null) {
            return CompletableFuture.supplyAsync(() -> {
                throw new Error();
            });
        }
        List<String> list = Arrays.asList(notes).stream().map((e) -> "\"" + e + "\"").collect(Collectors.toList());
        return JsonRequest.send(JsonQuery.fromLocalHost(port)
                .setParameter("json", String.format("{\"command\": \"chord_name\", \"notes\": [%s]}", String.join(",", list))), "GET");
    }

    /**
     * サーバーを停止します.
     *
     * @return
     */
    public Future<String> kill() {
        if (process == null) {
            return CompletableFuture.supplyAsync(() -> {
                throw new Error("alrady exit.");
            });
        }
        this.process = null;
        return JsonRequest.send(JsonQuery.fromLocalHost(port)
                .setParameter("json", "{\"command\": \"exit\"}"), "GET");
    }
}
