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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

class JsonQuery {

    private String baseAddress;
    private Map<String, String> parameters;

    public JsonQuery(String baseAddress) {
        this.baseAddress = baseAddress;
        this.parameters = new HashMap<>();
    }

    public static JsonQuery fromLocalHost(int port) {
        return new JsonQuery(String.format("http://localhost:%d/", port));
    }

    public JsonQuery setParameter(String key, Object val) {
        return setParameter(key, new Gson().toJson(val));
    }

    public JsonQuery setParameter(String key, String val) {
        parameters.put(key, val);
        return this;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String buildURL() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append(baseAddress);
        if (!baseAddress.endsWith("/")) {
            sb.append("/");
        }
        if (parameters.size() > 0) {
            sb.append("?");
            for (String key : parameters.keySet()) {
                String val = parameters.get(key);
                val = URLEncoder.encode(val, "UTF-8");
                sb.append(String.format("%s=%s", key, val));
                sb.append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
