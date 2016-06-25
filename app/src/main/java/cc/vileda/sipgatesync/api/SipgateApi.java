/*
 * Copyright 2016 vileda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.vileda.sipgatesync.api;

import android.support.annotation.NonNull;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public final class SipgateApi {
    private static final String HTTPS_API_SIPGATE_COM_V1 = "https://api.sipgate.com/v1";

    public static String getToken(final String username, final String password) {
        try {
            final HttpURLConnection urlConnection = getConnection("/authorization/token");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            JSONObject request = new JSONObject();
            request.put("username", username);
            request.put("password", password);
            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            Log.d("SipgateApi", request.getString("username"));
            wr.write(request.toString());
            wr.flush();
            StringBuilder sb = new StringBuilder();
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                Log.d("SipgateApi", "" + sb.toString());
                final JSONObject response = new JSONObject(sb.toString());
                return response.getString("token");
            }
            else {
                System.out.println(urlConnection.getResponseMessage());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @NonNull
    private static HttpURLConnection getConnection(String apiUrl) throws IOException {
        final URL url = new URL(HTTPS_API_SIPGATE_COM_V1 + apiUrl);
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");
        return urlConnection;
    }

    public static JSONArray getContacts(final String token) {
        try {
            return new JSONObject(getUrl("/contacts", token)).getJSONArray("items");
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @NonNull
    private static String getUrl(final String apiUrl, final String token) throws IOException {
        final HttpURLConnection urlConnection = getConnection(apiUrl);
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("GET");
        if (token != null) {
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);
        }
        StringBuilder sb = new StringBuilder();
        int HttpResult = urlConnection.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();

            return sb.toString();
        }
        else {
            System.out.println(urlConnection.getResponseMessage());
        }

        return "";
    }

    @NonNull
    private static String getUrl(final String apiUrl) throws IOException {
        return getUrl(apiUrl, null);
    }
}
