package javinator9889.bitcoinpools.NetTools

import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Created by Javinator9889 on 20/12/2017. Based on: https://stackoverflow.com/questions/1485708/how-do-i-do-a-http-get-in-java
 * Based on: https://stackoverflow.com/questions/1359689/how-to-send-http-request-in-java
 */

class net : AsyncTask<String, Void, JSONObject>() {

    override fun doInBackground(vararg url: String): JSONObject? {
        try {
            return getHttpsRequest(url[0])
        } catch (e: Exception) {
            return null
        }

    }

    companion object {
        @Throws(Exception::class)
        fun getHttpRequest(url: String): JSONObject {
            val response = StringBuilder()
            val urlObject = URL(url)
            val connection = urlObject.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val bufferedReader = BufferedReader(
                    InputStreamReader(connection.inputStream))

            var line: String
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line)
            }
            bufferedReader.close()
            return JSONObject(response.toString())
        }

        @Throws(Exception::class)
        fun getHttpsRequest(url: String): JSONObject {
            val response = StringBuilder()
            val urlObject = URL(url)
            val connection = urlObject.openConnection() as HttpsURLConnection
            connection.requestMethod = "GET"
            val bufferedReader = BufferedReader(
                    InputStreamReader(connection.inputStream))

            var line: String
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line)
            }
            bufferedReader.close()
            return JSONObject(response.toString())
        }

        fun isHostReachable(host: String, context: Context): Boolean {
            val TIMEOUT_IN_MS = 2000
            val RESPONSE_CODE_OK = 200
            if (host == "") return false
            val manager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = manager.activeNetworkInfo
            if (info != null && info.isConnected) {
                try {
                    val url = URL(host)
                    val urlConnection = url.openConnection() as HttpsURLConnection
                    urlConnection.connectTimeout = TIMEOUT_IN_MS
                    urlConnection.connect()
                    return urlConnection.responseCode == RESPONSE_CODE_OK
                } catch (ignored: Exception) {
                    return false
                }

            }
            return false
        }
    }
}
