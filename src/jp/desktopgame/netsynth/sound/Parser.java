/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Parser {

    private String source;
    private List<String> blocks;
    private String key;
    private int index;
    private int category;

    public Parser(String source) {
        this.source = source;
    }

    public void parse() {
        this.blocks = new ArrayList<>();
        // 名前を分割する
        final String delimiter = " -_";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            String c = source.substring(i, i + 1);
            if (delimiter.contains(c)) {
                blocks.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        if (sb.length() > 0) {
            blocks.add(sb.toString());
        }
        int digitsPos = blocks.size();
        // 後ろから整数をスキャン
        for (int i = blocks.size() - 1; i >= 0; i--) {
            String s = blocks.get(i);
            if (isDigitOnly(s)) {
                digitsPos = i;
            } else {
                break;
            }
        }
        this.category = digitsPos >= blocks.size() ? -1 : parseInt(blocks.get(digitsPos));
        this.index = -1;
        // 後ろからコードスキャン
        for (int i = digitsPos - 1; i >= 0; i--) {
            String s = blocks.get(i);
            if (isChord(s)) {
                this.key = s;
                this.index = category;
                break;
            } else {
                if (s.length() == 0) {
                    continue;
                }
                String chord = s.substring(0, s.length() - 1);
                String index = s.substring(s.length() - 1, s.length());
                //System.out.printf("%s %s\n", chord, index);
                if (!isChord(chord)) {
                    continue;
                }
                this.key = chord;
                this.index = parseInt(index);
                break;
            }
        }
        //System.out.printf("%s(%s) => %s %d %d\n", source, String.join(",", blocks), key, index, category);
    }

    public String getKey() {
        return key;
    }

    public int getIndex() {
        return index;
    }

    public int getCategory() {
        return category;
    }

    private int parseInt(String s) {
        if (s.length() == 0) {
            return -1;
        }
        if (s.charAt(0) == '0') {
            if (s.length() == 1) {
                return 0;
            }
            return parseInt(s.substring(1));
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean isChord(String s) {
        if (s.length() >= 3 || s.length() == 0) {
            return false;
        }
        s = s.toUpperCase();
        char c = s.charAt(0);
        List<Character> alphas = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G');
        if (alphas.contains(c)) {
            return s.length() == 1 || s.charAt(1) == '#';
        }
        return false;
    }

    private boolean isDigitOnly(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

}
