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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * サウンドをカテゴリ分けして階層毎に保存するデータベースです.
 *
 * @author desktopgame
 */
public class SoundDatabase {

    private SoundDatabase parent;
    private File directory;
    private List<SoundDatabase> subdatabse;
    private List<SoundEffect> effects;
    private static Map<String, SoundDatabase> sdbMap = new HashMap<>();

    private SoundDatabase(File directory) {
        this.directory = directory;
        this.subdatabse = new ArrayList<>();
        this.effects = new ArrayList<>();
    }

    public static SoundDatabase create(File rootDir) {
        SoundDatabase sdb = new SoundDatabase(rootDir);
        loadRec(sdb);
        sdbMap.put(rootDir.getPath(), sdb);
        return sdb;
    }

    public static Optional<SoundDatabase> get(String path) {
        final String[] arr = path.split("/");
        Optional<SoundDatabase> rootOpt = sdbMap.values().stream().filter((e) -> e.getName().equals(arr[0])).findFirst();
        if (rootOpt.isPresent()) {
            SoundDatabase ret = rootOpt.get();
            for (int i = 1; i < arr.length; i++) {
                final SoundDatabase retF = ret;
                final int index = i;
                Optional<SoundDatabase> o = retF.getSubDatabase()
                        .stream()
                        .filter((e) -> e.getName().equals(arr[index]))
                        .findFirst();
                if (o.isPresent()) {
                    ret = o.get();
                } else {
                    return Optional.empty();
                }
            }
            return Optional.of(ret);
        }
        return Optional.empty();
    }

    private static void loadRec(SoundDatabase sdb) {
        for (File file : sdb.directory.listFiles()) {
            if (file.isDirectory()) {
                Path path = Paths.get(sdb.directory.getPath(), file.getName());
                SoundDatabase childSdb = new SoundDatabase(path.toFile());
                childSdb.parent = sdb;
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

    public List<SoundDatabase> getIncludedSubDatabases(boolean includeSelf) {
        List<SoundDatabase> ret = new ArrayList<>();
        if (includeSelf) {
            ret.add(this);
        }
        getIncludedSubDatabasesImpl(this, ret);
        return ret;
    }

    private void getIncludedSubDatabasesImpl(SoundDatabase parent, List<SoundDatabase> dest) {
        for (SoundDatabase sdb : parent.subdatabse) {
            dest.add(sdb);
            getIncludedSubDatabasesImpl(sdb, dest);
        }
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        SoundDatabase parent = this;
        do {
            sb.append(parent.getName());
            parent = parent.parent;
            if (parent != null) {
                sb.append("/");
            }
        } while (parent != null);
        return sb.toString();
    }

}
