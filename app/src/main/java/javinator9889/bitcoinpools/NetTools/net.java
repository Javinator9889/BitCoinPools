package javinator9889.bitcoinpools.NetTools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;

/**
 * Created by Javinator9889 on 20/12/2017.
 * Based on: https://stackoverflow.com/questions/1485708/how-do-i-do-a-http-get-in-java
 * Based on: https://stackoverflow.com/questions/1359689/how-to-send-http-request-in-java
 */

public class net extends AsyncTask<String, Void, JSONObject> {
    @Override
    protected JSONObject doInBackground(String... url) {
        try {
            return getHttpsRequest(url[0]);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    public static JSONObject getHttpRequest(String url) throws Exception {
        StringBuilder response = new StringBuilder();
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            response.append(line);
        }
        bufferedReader.close();
        return new JSONObject(response.toString());
    }

    @NonNull
    public static JSONObject getHttpsRequest(String url) throws Exception {
        StringBuilder response = new StringBuilder();
        URL urlObject = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) urlObject.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            response.append(line);
        }
        bufferedReader.close();
        return new JSONObject(response.toString());
    }

    public static boolean isHostReachable(@NonNull String host, @NonNull Context context) {
        int TIMEOUT_IN_MS = 2000;
        int RESPONSE_CODE_OK = 200;
        if (host.equals("")) return false;
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            try {
                URL url = new URL(host);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(TIMEOUT_IN_MS);
                urlConnection.connect();
                return urlConnection.getResponseCode() == RESPONSE_CODE_OK;
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }
}
