/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.sound;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * サウンドをカテゴリ分けして階層毎に保存するデータベースです.
 *
 * @author desktopgame
 */
public class SoundDatabase {

    private File directory;
    private List<SoundDatabase> subdatabse;
    private List<SoundEffect> effects;

    private SoundDatabase(File directory) {
        this.directory = directory;
        this.subdatabse = new ArrayList<>();
        this.effects = new ArrayList<>();
    }

    public static SoundDatabase create(File rootDir) {
        SoundDatabase sdb = new SoundDatabase(rootDir);
        loadRec(sdb);
        return sdb;
    }

    private static void loadRec(SoundDatabase sdb) {
        for (File file : sdb.directory.listFiles()) {
            if (file.isDirectory()) {
                Path path = Paths.get(sdb.directory.getPath(), file.getName());
                SoundDatabase childSdb = new SoundDatabase(path.toFile());
                sdb.subdatabse.add(childSdb);
                loadRec(childSdb);
            } else {
                String name = file.getName();
                List<String> extensions = Arrays.asList(".aif", ".aiff", ".wav");
                if (extensions.stream().anyMatch((e) -> name.endsWith(e))) {
                    sdb.effects.add(new SoundEffect(new File(sdb.directory, file.getName())));
                }
            }
        }
    }

    public void dump(OutputStream out) {
        dump(new PrintStream(out), this, 0);
    }

    private static void dump(PrintStream out, SoundDatabase sdb, int d) {
        indent(out, d);
        out.println(sdb.getName());

        indent(out, d + 1);
        out.println("SE");
        for (SoundEffect se : sdb.effects) {
            indent(out, d + 2);
            out.println(se.getName());
        }

        indent(out, d + 1);
        out.println("Database");
        for (SoundDatabase schild : sdb.subdatabse) {
            dump(out, schild, d + 2);
        }

    }

    private static void indent(PrintStream out, int d) {
        for (int i = 0; i < d; i++) {
            out.print("    ");
        }
    }

    public List<SoundEffect> getAllEffects() {
        List<SoundEffect> dest = new ArrayList<>();
        getAllEffects(this, dest);
        return dest;
    }

    private void getAllEffects(SoundDatabase sdb, List<SoundEffect> dest) {
        dest.addAll(effects);
        for (SoundDatabase schild : sdb.subdatabse) {
            schild.getAllEffects(schild, dest);
        }
    }

    public List<SoundDatabase> getSubDatabase() {
        return new ArrayList<>(subdatabse);
    }

    public List<SoundEffect> getEffects() {
        return new ArrayList<>(effects);
    }

    public File getDirectory() {
        return directory;
    }

    public String getName() {
        return directory.getName();
    }

}
