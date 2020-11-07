/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.music21;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;

/**
 * Javaからpythonを操作するためのユーティリティです.
 *
 * @author desktopgame
 */
public class PythonUtil {

    private PythonUtil() {
    }

    private static Optional<String> wrap(String s) {
        if (s == null || s.equals("")) {
            return Optional.empty();
        }
        return Optional.of(s);
    }

    /**
     * システムに元からインストールされているpythonのパスを返します.
     *
     * @return
     */
    public static Optional<String> getSystemPythonPath() {
        try {
            Process proc = ProcessUtil.createShell("where python", "which python").start();
            proc.waitFor();
            StringWriter writer = new StringWriter();
            IOUtils.copy(proc.getInputStream(), writer, Charset.forName("UTF-8"));
            return wrap(writer.toString().trim());
        } catch (InterruptedException | IOException ex) {
            return Optional.empty();
        }
    }

    /**
     * pyenvのパスを返します.
     *
     * @return
     */
    public static Optional<String> getPyenvPath() {
        String homeDir = System.getenv("HOME");
        File f = new File(String.format("/usr/local/bin/pyenv", homeDir));
        if (f.exists()) {
            return Optional.of(f.getPath());
        }
        return Optional.empty();
    }

    /**
     * pyenvが使用するpythonをバージョンを指定せずに検索して返します.
     *
     * @return
     */
    public static Optional<String> getPyenvPythonPath() {
        return getPyenvPythonPath("");
    }

    /**
     * pyenvが使用するpythonを検索して返します.
     *
     * @param version
     * @return
     */
    public static Optional<String> getPyenvPythonPath(String version) {
        Optional<String> pathOpt = getPyenvPath().map((pyenv) -> {
            try {
                String cmd = String.format("%s shims", pyenv);
                Process proc = ProcessUtil.createShell(cmd, cmd).start();
                proc.waitFor();
                StringWriter outW = new StringWriter();
                StringWriter errW = new StringWriter();
                IOUtils.copy(proc.getErrorStream(), errW, Charset.forName("UTF-8"));
                String x = errW.toString();
                IOUtils.copy(proc.getInputStream(), outW, Charset.forName("UTF-8"));
                List<String> lines = Arrays.asList(outW.toString().split(System.lineSeparator()));
                String name = String.format("python%s", version);
                return lines.stream().filter((e) -> e.endsWith(name)).findFirst().map((e) -> e.trim()).orElse("");
            } catch (InterruptedException | IOException ex) {
                return "";
            }
        });
        if (pathOpt.isPresent()) {
            String path = pathOpt.get();
            return wrap(path);
        }
        return Optional.empty();
    }
}
