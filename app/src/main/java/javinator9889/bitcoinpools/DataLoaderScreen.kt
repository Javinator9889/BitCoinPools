package javinator9889.bitcoinpools

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.perf.metrics.AddTrace
import javinator9889.bitcoinpools.JSONTools.JSONTools
import javinator9889.bitcoinpools.NetTools.net
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

/**
 * Created by Javinator9889 on 04/03/2018. Based on: https://stackoverflow.com/questions/10115403/progressdialog-while-load-activity
 */

class DataLoaderScreen : BaseActivity() {

    private var mpu: Float = 0.toFloat()
    private var retrievedData: HashMap<String, Float>? = null
    private var cardsData: HashMap<String, Float>? = null
    private var btcPrice: HashMap<Date, Float>? = null
    private var mContext: Context? = null

    @AddTrace(name = "getApplicationValues")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataLoaderScreenActivity = this
        setContentView(R.layout.activity_loading)
        mContext = applicationContext
        if (BitCoinApp.isOnline) {
            Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_MAINVIEW)
            DataLoader().execute()
        } else {
            try {
                MaterialDialog.Builder(this)
                        .title(R.string.noConnectionTitle)
                        .content(R.string.noConnectionDesc)
                        .cancelable(false)
                        .positiveText(R.string.accept)
                        .onPositive { dialog, which -> onBackPressed() }
                        .build()
                        .show()
            } catch (e: Exception) {
                // MaterialDialog lib doesn't provide functionality to catch its own exception
                // "DialogException" so we need to catch a global generally exception and cancel
                // app execution. This happens because the application is trying to show a dialog
                // after the app has been closed (there is no activity).
                Log.e("MaterialDialog", "Not possible to show dialog - maybe the app is" +
                        " closed. Full trace: " + e.message)
                try {
                    this.finish()
                } catch (activityFinishException: Exception) {
                    // Maybe closing the activity when execution failed throws an exception if
                    // there is no possibility to close it
                    Log.e("FinishException", "Impossible to finish the activity. Maybe " +
                            "it is finished yet. More info: "
                            + activityFinishException.message)
                }

            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class DataLoader : AsyncTask<Void, Void, Boolean>() {
        private var marketPriceThread: Thread? = null
        private var poolsDataThread: Thread? = null
        private var cardsDataThread: Thread? = null
        private var btcPriceThread: Thread? = null
        private var isAnyExceptionThrown = false
        private var areHostsReachable = true
        private val threadExceptions = Thread.UncaughtExceptionHandler { t, e ->
            isAnyExceptionThrown = true
            Log.e("DataLoaderScreen", "Exception on thread: " + t.name
                    + " | Message: " + e.message)
            FirebaseCrash.log("DataLoaderScreen. Exception on thread: " + t.name
                    + " | Message: " + e.message)
            if (e is HostNonReachableException) {
                areHostsReachable = false
            }
        }

        override fun onPreExecute() {}

        override fun onPostExecute(result: Boolean?) {
            if (result!! && !isAnyExceptionThrown) {
                val activityMainIntent = Intent(this@DataLoaderScreen,
                        MainActivity::class.java)
                activityMainIntent.putExtra("MPU", mpu)
                activityMainIntent.putExtra("RD", retrievedData)
                activityMainIntent.putExtra("CARDS", cardsData)
                activityMainIntent.putExtra("BTCPRICE", btcPrice)
                startActivity(activityMainIntent)
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
            } else if (!areHostsReachable) {
                try {
                    MaterialDialog.Builder(mContext!!)
                            .title(R.string.host_not_available_title)
                            .content(R.string.host_not_available_desc)
                            .cancelable(false)
                            .positiveText(R.string.accept)
                            .onPositive { dialog, which -> onBackPressed() }
                            .build()
                            .show()
                } catch (e: Exception) {
                    try {
                        finish()
                    } catch (ignored: Exception) {
                    }

                }

            } else {
                if (BitCoinApp.isOnline) {
                    Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_MAINVIEW)
                    DataLoader().execute()
                } else {
                    try {
                        MaterialDialog.Builder(this@DataLoaderScreen)
                                .title(R.string.noConnectionTitle)
                                .content(R.string.noConnectionDesc)
                                .cancelable(false)
                                .positiveText(R.string.accept)
                                .onPositive { dialog, which -> onBackPressed() }
                                .build()
                                .show()
                    } catch (e: Exception) {
                        // MaterialDialog lib doesn't provide functionality to catch its own
                        // exception
                        // "DialogException" so we need to catch a global generally exception and
                        // cancel
                        // app execution. This happens because the application is trying to show
                        // a dialog
                        // after the app has been closed (there is no activity).
                        Log.e("MaterialDialog",
                                "Not possible to show dialog - maybe the app is" +
                                        " closed. Full trace: " + e.message)
                        try {
                            this@DataLoaderScreen.finish()
                        } catch (activityFinishException: Exception) {
                            // Maybe closing the activity when execution failed throws an exception
                            // if there is no possibility to close it
                            Log.e("FinishException",
                                    "Impossible to finish the activity. Maybe it is finished" +
                                            " yet. More info: "
                                            + activityFinishException.message)
                        }

                    }

                }
                Log.e("DataLoaderScreen", "An exception was thrown. Trying to obtain " + "data again")
            }
        }

        override fun doInBackground(vararg params: Void): Boolean? {
            try {
                getBitCoinMarketPrice()
                getPoolsData()
                getCardsData()
                getBitCoinPriceHistory()
                marketPriceThread!!.join()
                poolsDataThread!!.join()
                cardsDataThread!!.join()
                btcPriceThread!!.join()
                return true
            } catch (e: InterruptedException) {
                Log.e(Constants.LOG.MATAG, Constants.LOG.JOIN_ERROR + e.message)
                isAnyExceptionThrown = true
                return false
            } catch (e: DataLoaderException) {
                Log.e(Constants.LOG.MATAG, Constants.LOG.JOIN_ERROR + e.message)
                isAnyExceptionThrown = true
                return false
            }

        }

        @Throws(Exception::class)
        private fun getHTTPSRequest(requestUrl: String): JSONObject {
            val response = StringBuilder()
            val urlObject = URL(requestUrl)
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

        @Throws(DataLoaderScreen.DataLoaderException::class)
        private fun getBitCoinMarketPrice() {
            marketPriceThread = Thread(Runnable {
                Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_MPU)
                if (!net.isHostReachable(Constants.STATS_URL, mContext!!))
                    throw HostNonReachableException(String.format("The URL \"%s\" is not " + "reachable", Constants.STATS_URL))
                try {
                    mpu = round(getHTTPSRequest(Constants.STATS_URL)
                            .getDouble(Constants.MARKET_NAME).toFloat(), 2)
                } catch (e: Exception) {
                    Log.e(Constants.LOG.MATAG,
                            Constants.LOG.MARKET_PRICE_ERROR + e.message)
                    mpu = -1f
                    throw DataLoaderException(
                            "Failed to get data from: " + Constants.STATS_URL)
                }
            })
            marketPriceThread!!.uncaughtExceptionHandler = threadExceptions
            marketPriceThread!!.name = "MarketPriceThread"
            marketPriceThread!!.start()
        }

        private fun getPoolsData() {
            poolsDataThread = Thread(Runnable {
                val days = BitCoinApp.sharedPreferences!!
                        .getInt(Constants.SHARED_PREFERENCES.DAYS_TO_CHECK, 1)
                Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_RD)
                val url = Constants.POOLS_URL + days + "days"
                if (!net.isHostReachable(url, mContext!!))
                    throw HostNonReachableException(String.format("The URL \"%s\" is not " + "reachable", url))
                try {
                    retrievedData = JSONTools.convert2HashMap(getHTTPSRequest(url))
                } catch (e: Exception) {
                    retrievedData = null
                    Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.message)
                    throw DataLoaderException("Unable to get data from: $url")
                }
            })
            poolsDataThread!!.uncaughtExceptionHandler = threadExceptions
            poolsDataThread!!.name = "PoolsDataThread"
            poolsDataThread!!.start()
        }

        private fun getCardsData() {
            cardsDataThread = Thread(Runnable {
                if (!net.isHostReachable(Constants.STATS_URL, mContext!!))
                    throw HostNonReachableException(String.format("The URL \"%s\" is not " + "reachable", Constants.STATS_URL))
                try {
                    cardsData = JSONTools.convert2HashMap(getHTTPSRequest(Constants.STATS_URL))
                } catch (e: Exception) {
                    cardsData = null
                    Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.message)
                    throw DataLoaderException(
                            "Unable to get data from: " + Constants.STATS_URL)
                }
            })
            cardsDataThread!!.uncaughtExceptionHandler = threadExceptions
            cardsDataThread!!.name = "CardsDataThread"
            cardsDataThread!!.start()
        }

        private fun getBitCoinPriceHistory() {
            btcPriceThread = Thread(Runnable {
                if (!net.isHostReachable(Constants.API_URL, mContext!!))
                    throw HostNonReachableException(String.format("The URL \"%s\" is not " + "reachable", Constants.API_URL))
                try {
                    btcPrice = JSONTools.convert2DateHashMap(getHTTPSRequest(Constants.API_URL)
                            .getJSONObject("bpi"))
                } catch (e: Exception) {
                    btcPrice = null
                    Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.message)
                    throw DataLoaderException(
                            "Unable to get data from: " + Constants.API_URL)
                }
            })
            btcPriceThread!!.uncaughtExceptionHandler = threadExceptions
            btcPriceThread!!.name = "BtcPriceThread"
            btcPriceThread!!.start()
        }
    }

    internal inner class DataLoaderException(message: String) : RuntimeException(message)

    internal inner class HostNonReachableException(message: String) : RuntimeException(message)

    companion object {
        var dataLoaderScreenActivity: AppCompatActivity
    }
}
