package javinator9889.bitcoinpools.BackgroundJobs

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import com.googlecode.charts4j.*
import javinator9889.bitcoinpools.*
import javinator9889.bitcoinpools.JSONTools.JSONTools
import javinator9889.bitcoinpools.NetTools.net
import org.json.JSONException
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Created by Javinator9889 on 23/01/2018. Based on: https://stackoverflow.com/a/46991229
 */

internal class NotificationHandler private constructor() {

    init {
        val sp = BitCoinApp.getSharedPreferences()
        Log.d(INSTANCE.getNTAG(), Constants.LOG.CREATING_NOTIFICATION)
        NOTIFICATIONS_ENABLED = sp.getBoolean(Constants.SHARED_PREFERENCES.NOTIFICATIONS_ENABLED,
                false)
        NOTIFIED_HIGH = sp.getBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_HIGH, false)
        NOTIFIED_LOW = sp.getBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_LOW, false)
        if (sp.getBoolean(INSTANCE.getHAS_USER_DEFINED_CUSTOM_PRICE(), false))
            SPECIFIC_VALUE = sp.getFloat(INSTANCE.getCUSTOM_PRICE(), 10f)
        else
            SPECIFIC_VALUE = sp.getInt(Constants.SHARED_PREFERENCES.VALUE_TO_CHECK, 1000).toFloat()
        MPU = initMPU()
        Log.d(INSTANCE.getNTAG(), Constants.LOG.CURRRENT_NOT_SETTINGS
                + NOTIFICATIONS_ENABLED + "\n"
                + NOTIFIED_HIGH + "\n"
                + NOTIFIED_LOW + "\n"
                + SPECIFIC_VALUE + "\n"
                + MPU)
    }

    fun putNotification() {
        var notify = false
        var notificationTitle = ""
        var notificationText = ""
        var notificationTextLong = ""
        val notificationManager = BitCoinApp.getAppContext()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val clickIntent = PendingIntent.getActivity(
                BitCoinApp.getAppContext(),
                Constants.REQUEST_CODE,
                Intent(BitCoinApp.getAppContext(), DataLoaderScreen::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val localeContext = BitCoinApp.localeManager.setLocale(BitCoinApp.getAppContext())
        if (NOTIFICATIONS_ENABLED) {
            if (MPU < SPECIFIC_VALUE && !NOTIFIED_LOW) {
                notificationTitle = localeContext.getString(R.string.lowerPrice)
                notificationTextLong = (localeContext.getString(R.string.lowerPriceX)
                        + SPECIFIC_VALUE + ". "
                        + localeContext.getString(R.string.actualCost) + MPU)
                notificationText = localeContext.getString(R.string.lowerPriceX) + SPECIFIC_VALUE
                NOTIFIED_HIGH = false
                NOTIFIED_LOW = true
                notify = MPU != -1f
            } else if (MPU > SPECIFIC_VALUE && !NOTIFIED_HIGH) {
                notificationTitle = localeContext.getString(R.string.morePrice)
                notificationTextLong = (localeContext.getString(R.string.morePriceX)
                        + SPECIFIC_VALUE + ". " + localeContext.getString(R.string.actualCost) + MPU)

                notificationText = localeContext.getString(R.string.morePriceX) + SPECIFIC_VALUE
                NOTIFIED_HIGH = true
                NOTIFIED_LOW = false
                notify = MPU != -1f
            }
            val notificationTitleLong = localeContext.getString(R.string.actualCost) + MPU
            if (notify) {
                Log.d(INSTANCE.getNTAG(), Constants.LOG.NOTIFYING)
                updatePreferences()
                val lineChartTask = generateLineChart(localeContext)
                val name = localeContext.getString(R.string.alerts)
                val description = localeContext.getString(R.string.description)
                val notification: Notification.Builder

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = android.app.NotificationManager.IMPORTANCE_HIGH
                    assert(notificationManager != null)
                    var mChannel: NotificationChannel? = notificationManager
                            .getNotificationChannel(Constants.CHANNEL_ID)
                    if (mChannel == null) {
                        mChannel = NotificationChannel(Constants.CHANNEL_ID, name, importance)
                        mChannel.description = description
                        mChannel.enableVibration(true)
                        mChannel.vibrationPattern = longArrayOf(100, 100, 300)
                        notificationManager.createNotificationChannel(mChannel)
                    }

                    notification = Notification.Builder(BitCoinApp.getAppContext(),
                            Constants.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_stat_equalizer)
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setContentTitle(notificationTitle)
                            .setContentText(notificationText)
                            .setAutoCancel(true)
                            .setTicker(notificationTitle)
                            .setStyle(Notification.BigTextStyle()
                                    .bigText(notificationTextLong))
                } else {
                    notification = Notification.Builder(BitCoinApp.getAppContext())
                            .setSmallIcon(R.drawable.ic_stat_equalizer)
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setContentTitle(notificationTitle)
                            .setContentText(notificationText)
                            .setAutoCancel(true)
                            .setTicker(notificationTitle)
                            .setVibrate(longArrayOf(100, 100, 300))
                            .setStyle(Notification.BigTextStyle()
                                    .bigText(notificationTextLong))
                }
                try {
                    if (lineChartTask != null) {
                        val lineChart = lineChartTask.get()
                        if (lineChart != null) {
                            notification.style = Notification.BigPictureStyle()
                                    .bigPicture(lineChart)
                                    .bigLargeIcon(null as Bitmap?)
                                    .setBigContentTitle(notificationTitleLong)
                            notification.setLargeIcon(lineChart)
                            val dataIntent = ShareDataIntent(localeContext,
                                    R.string.share_bitcoin_price_title)
                            if (dataIntent.saveImageToCache(lineChart)) {
                                val fileUri = dataIntent.uriForSavedImage
                                val googlePlayLink = Uri.parse(Constants.GOOGLE_PLAY_URL)
                                val bitCoinPriceChangedText = String.format(
                                        localeContext.getString(R.string.share_bitcoin_price),
                                        notificationText,
                                        MPU,
                                        googlePlayLink.toString())
                                val shareIntent = dataIntent.shareImageWithText(bitCoinPriceChangedText, fileUri)
                                val action = Notification.Action(R.drawable.share,
                                        localeContext.getString(R.string.share), shareIntent)
                                notification.addAction(action)
                            }
                        }
                    }
                } catch (e: ExecutionException) {
                    Log.w(INSTANCE.getNTAG(), "Unexpected exception while publishing " + "notification", e)
                } catch (e: InterruptedException) {
                    Log.w(INSTANCE.getNTAG(), "Unexpected exception while publishing " + "notification", e)
                } finally {
                    notification.setContentIntent(clickIntent)
                    assert(notificationManager != null)
                    notificationManager.notify(Constants.NOTIFICATION_ID, notification.build())
                }
            }
        } else
            Log.d(INSTANCE.getNTAG(), Constants.LOG.NNOTIFYING)
    }

    private fun generateLineChart(localeContext: Context): AsyncTask<String, Void, Bitmap>? {
        val pricesMap: Map<Date, Float>?
        val start = Calendar.getInstance()
        start.add(Calendar.DAY_OF_MONTH, -7)
        val startDate = String.format(Locale.US, "%d-%02d-%02d",
                start.get(Calendar.YEAR),
                start.get(Calendar.MONTH) + 1,
                start.get(Calendar.DAY_OF_MONTH))
        val url = INSTANCE.getAPI_URL() + "?start=" + startDate + "&end=" +
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)
        pricesMap = getValuesByDatedURL(url)
        if (pricesMap == null)
            return null

        val divider = getDivider(pricesMap)
        val bitcoinPricesLine = Plots.newLine(getNormalizedData(pricesMap), Color.DARKBLUE)
        bitcoinPricesLine.setLineStyle(LineStyle.MEDIUM_DOTTED_LINE)
        bitcoinPricesLine.addShapeMarkers(Shape.CIRCLE, Color.BLACK, 8)
        bitcoinPricesLine.setFillAreaColor(Color.CYAN)

        val bitcoinPricesChart = GCharts.newLineChart(bitcoinPricesLine)
        bitcoinPricesChart.setSize(760, 380)
        bitcoinPricesChart.setGrid(25.0, 25.0, 3, 2)
        bitcoinPricesChart.setTitle(localeContext.getString(R.string.chart_title))

        val axisStyle = AxisStyle.newAxisStyle(Color.BLACK, 11, AxisTextAlignment.CENTER)
        val xAxis = AxisLabelsFactory.newAxisLabels(getDates(pricesMap))
        val yAxis = AxisLabelsFactory.newAxisLabels(getValues(pricesMap, divider))
        xAxis.setAxisStyle(axisStyle)
        yAxis.setAxisStyle(axisStyle)

        bitcoinPricesChart.addXAxisLabels(xAxis)
        bitcoinPricesChart.addYAxisLabels(yAxis)
        bitcoinPricesChart.setBackgroundFill(Fills.newSolidFill(Color.WHITE))
        val downloader = ImageDownloader()
        return downloader.execute(bitcoinPricesChart.toURLString())
    }

    private fun getDivider(pricesMap: Map<Date, Float>): Int {
        var maximum: Float? = Collections.max(pricesMap.values)
        var iterations = 0
        while (maximum >= 10) {
            maximum /= 10
            ++iterations
        }
        return Math.pow(10.0, iterations.toDouble()).toInt()
    }

    private fun getDates(pricesMap: Map<Date, Float>): List<String> {
        val dates = ArrayList<String>(pricesMap.keys.size)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (currentDate in pricesMap.keys)
            dates.add(formatter.format(currentDate))
        return dates
    }

    private fun getValues(pricesMap: Map<Date, Float>, divider: Int): List<String> {
        val values = pricesMap.values
        val formattedValues = ArrayList<String>(6)
        val maximum = Collections.max(values)
        val minimum = Collections.min(values)
        val difference = maximum - minimum
        val amount = difference / 5
        formattedValues.add(0, String.format(Locale.US, "$%.2fK", minimum / divider))
        var latest = minimum
        for (i in 1..4) {
            latest += amount
            formattedValues.add(i, String.format(Locale.US, "$%.2fK", latest / divider))
        }
        formattedValues.add(5, String.format(Locale.US, "$%.2fK", maximum / divider))
        return formattedValues
    }

    private fun getNormalizedData(pricesMap: Map<Date, Float>): Data {
        val normalizedValues = DoubleArray(pricesMap.values.size)
        val maximum = Collections.max(pricesMap.values)
        val minimum = Collections.min(pricesMap.values)
        val difference = maximum - minimum
        var i = 0
        for (currentValue in pricesMap.values) {
            val actualDifference = currentValue - minimum
            normalizedValues[i] = (100 * actualDifference / difference).toDouble()
            ++i
        }
        return Data.newData(*normalizedValues)
    }

    private fun getValuesByDatedURL(url: String): Map<Date, Float>? {
        val netRequest = net()
        netRequest.execute(url)
        try {
            return JSONTools.sortDateByValue(
                    JSONTools.convert2DateHashMap(netRequest.get().getJSONObject("bpi")))
        } catch (ignored: InterruptedException) {
            return null
        } catch (ignored: ExecutionException) {
            return null
        } catch (ignored: JSONException) {
            return null
        } catch (ignored: NullPointerException) {
            return null
        }

    }

    fun updatePreferences() {
        updateSharedPreferences()
    }

    private class ImageDownloader : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg urls: String): Bitmap? {
            try {
                val httpImageUrl = URL(urls[0])
                val httpsImageUrl = URL("https", httpImageUrl.host, httpImageUrl.file)
                Log.d(INSTANCE.getNTAG(), "URL: $httpsImageUrl")
                return BitmapFactory.decodeStream(httpsImageUrl.openConnection().getInputStream())
            } catch (ex: IOException) {
                Log.w(INSTANCE.getNTAG(), "Error while downloading the photo", ex)
                return null
            }

        }

    }

    companion object {
        private var NOTIFICATIONS_ENABLED = false
        private var NOTIFIED_HIGH = false
        private var NOTIFIED_LOW = false
        private var SPECIFIC_VALUE = 0f
        private var MPU: Float

        fun newInstance(): NotificationHandler {
            return NotificationHandler()
        }

        private fun initMPU(): Float {
            val market = net()
            market.execute("https://api.blockchain.info/stats")
            try {
                return MainActivity.round(market.get().getDouble("market_price_usd").toFloat(),
                        2)
            } catch (e: InterruptedException) {
                return -1f
            } catch (e: ExecutionException) {
                return -1f
            } catch (e: JSONException) {
                return -1f
            } catch (e: NullPointerException) {
                return -1f
            }

        }

        private fun updateSharedPreferences() {
            val sharedPreferencesEditor = BitCoinApp
                    .getSharedPreferences().edit()
            sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_HIGH,
                    NOTIFIED_HIGH)
            sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_LOW,
                    NOTIFIED_LOW)
            sharedPreferencesEditor.apply()
        }
    }
}
