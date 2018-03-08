package javinator9889.bitcoinpools.NetTools;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

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
}
