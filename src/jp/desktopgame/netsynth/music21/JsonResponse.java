/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.music21;

import com.google.gson.Gson;
import java.util.Optional;

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

    /**
     * 対象のJSON文字列をJsonResponseとしてパースして結果が成功だったときのみTでパースして結果を返します.
     *
     * @param <T>
     * @param json
     * @param c
     * @return
     */
    public static <T> Optional<T> fromJson(String json, Class<T> c) {
        if (json == null) {
            return Optional.empty();
        }
        JsonResponse resp = new Gson().fromJson(json, JsonResponse.class);
        if (resp == null || resp.status != 0) {
            return Optional.empty();
        }
        return Optional.of(new Gson().fromJson(json, c));
    }
}
