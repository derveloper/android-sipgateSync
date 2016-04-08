package cc.vileda.sipgatesync.sipgatesync.api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by vileda on 08.04.16.
 */
public final class SipgateApi {
    public static String getToken(final String username, final String password) {
        try {
            final URL url = new URL("https://api.sipgate.com/v1/authorization/token");
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestMethod("POST");
            JSONObject request = new JSONObject();
            request.put("username", username);
            request.put("password", password);
            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            Log.d("SipgateApi", request.toString());
            wr.write(request.toString());
            wr.flush();
            StringBuilder sb = new StringBuilder();
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                Log.d("SipgateApi", "" + sb.toString());
                final JSONObject response = new JSONObject(sb.toString());
                return response.getString("token");
            } else {
                System.out.println(urlConnection.getResponseMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONArray getUsers(final String token) {
        try {
            final URL url = new URL("https://api.sipgate.com/v1/users");
            final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer " + token);
            urlConnection.setRequestMethod("GET");
            StringBuilder sb = new StringBuilder();
            int HttpResult = urlConnection.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                Log.d("SipgateApi", "" + sb.toString());
                return new JSONObject(sb.toString()).getJSONArray("items");
            } else {
                System.out.println(urlConnection.getResponseMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
