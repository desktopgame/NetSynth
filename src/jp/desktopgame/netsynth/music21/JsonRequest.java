/*
 * NetSynth
 *
 * Copyright (c) 2020 desktopgame
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/mit-license.php
 */
package jp.desktopgame.netsynth.music21;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class JsonRequest {

    private static ExecutorService service;

    static {
        service = Executors.newSingleThreadExecutor();
    }

    private JsonRequest() {
    }

    private static String send(String _url, String method) throws MalformedURLException, IOException {
        URL url = new URL(_url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException();
        }
        InputStream in = connection.getInputStream();
        String encoding = connection.getContentEncoding();
        if (null == encoding) {
            encoding = "UTF-8";
        }
        StringBuffer result = new StringBuffer();
        final InputStreamReader inReader = new InputStreamReader(in, encoding);
        final BufferedReader bufReader = new BufferedReader(inReader);
        String line = null;
        while ((line = bufReader.readLine()) != null) {
            result.append(line);
        }
        bufReader.close();
        inReader.close();
        in.close();
        return result.toString();
    }

    public static Future<String> send(JsonQuery query, String method) {
        return service.submit(() -> {
            return send(query.buildURL(), method);
        });
    }
}
