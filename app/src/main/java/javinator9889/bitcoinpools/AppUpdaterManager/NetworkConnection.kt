package javinator9889.bitcoinpools.AppUpdaterManager

import android.os.AsyncTask
import android.util.Log
import javinator9889.bitcoinpools.Constants
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import java.nio.charset.Charset

/**
 * Created by Javinator9889 on 24/01/2018. Get HTTPS response from GitHub releases
 */

class NetworkConnection : AsyncTask<String, Void, JSONArray>() {

    override fun doInBackground(vararg url: String): JSONArray? {
        try {
            Log.d(Constants.LOG.NCTAG, Constants.LOG.CONNECTION)
            return readJSONFromURL(url[0])
        } catch (e: IOException) {
            Log.e(Constants.LOG.NCTAG, Constants.LOG.JSONERROR + e.message)
        } catch (e: JSONException) {
            Log.e(Constants.LOG.NCTAG, Constants.LOG.JSONERROR + e.message)
        }

        return null
    }

    @Throws(IOException::class)
    private fun readAll(httpsReader: Reader): String {
        val response = StringBuilder()
        var valueRead: Int
        while ((valueRead = httpsReader.read()) != -1) {
            response.append(valueRead.toChar())
        }
        return response.toString()
    }

    @Throws(IOException::class, JSONException::class)
    private fun readJSONFromURL(url: String): JSONArray {
        URL(url).openStream().use { JSONStream ->
            val br = BufferedReader(
                    InputStreamReader(JSONStream, Charset.forName("UTF-8")))
            val JSONText = readAll(br)
            return JSONArray(JSONText)
        }
    }
}
