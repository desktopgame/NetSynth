/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.music21;

/**
 * Music21から返却されるJSONの最小表現です.
 *
 * @author desktopgame
 */
public class JsonResponse {

    /**
     * この値が 0 であるなら処理の実行に成功しているので、他のパラメータを取得するためにJSON文字列をより具体的なデータ型でパースできます.
     */
    public int status;
}
