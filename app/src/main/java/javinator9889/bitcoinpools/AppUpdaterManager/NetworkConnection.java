package javinator9889.bitcoinpools.AppUpdaterManager;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import javinator9889.bitcoinpools.Constants;

/**
 * Created by Javinator9889 on 24/01/2018.
 * Get HTTPS response from GitHub releases
 */

public class NetworkConnection extends AsyncTask <String, Void, JSONArray> {

    @Override
    protected JSONArray doInBackground(String... url) {
        try {
            Log.d(Constants.LOG.NCTAG, Constants.LOG.CONNECTION);
            return readJSONFromURL(url[0]);
        } catch (IOException | JSONException e) {
            Log.e(Constants.LOG.NCTAG, Constants.LOG.JSONERROR + e.getMessage());
        }
        return null;
    }

    @NonNull
    private String readAll(Reader httpsReader) throws IOException {
        StringBuilder response = new StringBuilder();
        int valueRead;
        while ((valueRead = httpsReader.read()) != -1) {
            response.append((char) valueRead);
        }
        return response.toString();
    }

    @NonNull
    private JSONArray readJSONFromURL(String url) throws IOException, JSONException {
        try (InputStream JSONStream = new URL(url).openStream()) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(JSONStream, Charset.forName("UTF-8")));
            String JSONText = readAll(br);
            return new JSONArray(JSONText);
        }
    }
}
