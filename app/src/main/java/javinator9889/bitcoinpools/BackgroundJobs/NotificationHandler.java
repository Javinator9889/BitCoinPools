package javinator9889.bitcoinpools.BackgroundJobs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.Fills;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Line;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.LineStyle;
import com.googlecode.charts4j.Plots;
import com.googlecode.charts4j.Shape;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import javinator9889.bitcoinpools.BitCoinApp;
import javinator9889.bitcoinpools.Constants;
import javinator9889.bitcoinpools.DataLoaderScreen;
import javinator9889.bitcoinpools.JSONTools.JSONTools;
import javinator9889.bitcoinpools.MainActivity;
import javinator9889.bitcoinpools.NetTools.net;
import javinator9889.bitcoinpools.R;

import static javinator9889.bitcoinpools.Constants.API_URL;
import static javinator9889.bitcoinpools.Constants.LOG.NTAG;
import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.CUSTOM_PRICE;
import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.HAS_USER_DEFINED_CUSTOM_PRICE;

/**
 * Created by Javinator9889 on 23/01/2018. Based on: https://stackoverflow.com/a/46991229
 */

class NotificationHandler {
    private static boolean NOTIFICATIONS_ENABLED = false;
    private static boolean NOTIFIED_HIGH = false;
    private static boolean NOTIFIED_LOW = false;
    private static float SPECIFIC_VALUE = 0f;
    private static float MPU;

    private NotificationHandler() {
        final SharedPreferences sp = BitCoinApp.getSharedPreferences();
        Log.d(NTAG, Constants.LOG.CREATING_NOTIFICATION);
        NOTIFICATIONS_ENABLED = sp.getBoolean(Constants.SHARED_PREFERENCES.NOTIFICATIONS_ENABLED,
                false);
        NOTIFIED_HIGH = sp.getBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_HIGH, false);
        NOTIFIED_LOW = sp.getBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_LOW, false);
        if (sp.getBoolean(HAS_USER_DEFINED_CUSTOM_PRICE, false))
            SPECIFIC_VALUE = sp.getFloat(CUSTOM_PRICE, 10);
        else
            SPECIFIC_VALUE = sp.getInt(Constants.SHARED_PREFERENCES.VALUE_TO_CHECK, 1000);
        MPU = initMPU();
        Log.d(NTAG, Constants.LOG.CURRRENT_NOT_SETTINGS
                + NOTIFICATIONS_ENABLED + "\n"
                + NOTIFIED_HIGH + "\n"
                + NOTIFIED_LOW + "\n"
                + SPECIFIC_VALUE + "\n"
                + MPU);
    }

    @NonNull
    static NotificationHandler newInstance() {
        return new NotificationHandler();
    }

    private static float initMPU() {
        net market = new net();
        market.execute("https://api.blockchain.info/stats");
        try {
            return MainActivity.round((float) market.get().getDouble("market_price_usd"),
                    2);
        } catch (InterruptedException | ExecutionException
                | JSONException | NullPointerException e) {
            return -1;
        }
    }

    private static void updateSharedPreferences() {
        final SharedPreferences.Editor sharedPreferencesEditor = BitCoinApp
                .getSharedPreferences().edit();
        sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_HIGH,
                NOTIFIED_HIGH);
        sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_LOW,
                NOTIFIED_LOW);
        sharedPreferencesEditor.apply();
    }

    void putNotification() {
        boolean notify = false;
        String notificationTitle = "";
        String notificationText = "";
        String notificationTextLong = "";
        NotificationManager notificationManager = (NotificationManager) BitCoinApp.getAppContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent clickIntent = PendingIntent.getActivity(
                BitCoinApp.getAppContext(),
                Constants.REQUEST_CODE,
                new Intent(BitCoinApp.getAppContext(), DataLoaderScreen.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        Context localeContext = BitCoinApp.localeManager.setLocale(BitCoinApp.getAppContext());
        if (NOTIFICATIONS_ENABLED) {
            if ((MPU < SPECIFIC_VALUE) && !NOTIFIED_LOW) {
                notificationTitle = localeContext.getString(R.string.lowerPrice);
                notificationTextLong = localeContext.getString(R.string.lowerPriceX)
                        + SPECIFIC_VALUE + ". "
                        + localeContext.getString(R.string.actualCost) + MPU;
                notificationText = localeContext.getString(R.string.lowerPriceX)
                        + SPECIFIC_VALUE;
                NOTIFIED_HIGH = false;
                NOTIFIED_LOW = true;
                notify = (MPU != -1);
            } else if ((MPU > SPECIFIC_VALUE) && !NOTIFIED_HIGH) {
                notificationTitle = localeContext.getString(R.string.morePrice);
                notificationTextLong = localeContext.getString(R.string.morePriceX)
                        + SPECIFIC_VALUE + ". " + localeContext.getString(R.string.actualCost) + MPU;

                notificationText = localeContext.getString(R.string.morePriceX)
                        + SPECIFIC_VALUE;
                NOTIFIED_HIGH = true;
                NOTIFIED_LOW = false;
                notify = (MPU != -1);
            }
            String notificationTitleLong = localeContext.getString(R.string.actualCost) + MPU;
            if (notify) {
                Log.d(NTAG, Constants.LOG.NOTIFYING);
                updatePreferences();
                AsyncTask<String, Void, Bitmap> lineChartTask = generateLineChart(localeContext);
                String name = localeContext.getString(R.string.alerts);
                String description = localeContext.getString(R.string.description);
                Notification.Builder notification;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
                    assert notificationManager != null;
                    NotificationChannel mChannel = notificationManager
                            .getNotificationChannel(Constants.CHANNEL_ID);
                    if (mChannel == null) {
                        mChannel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
                        mChannel.setDescription(description);
                        mChannel.enableVibration(true);
                        mChannel.setVibrationPattern(new long[]{100, 100, 300});
                        notificationManager.createNotificationChannel(mChannel);
                    }

                    notification = new Notification.Builder(BitCoinApp.getAppContext(),
                            Constants.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_stat_equalizer)
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setContentTitle(notificationTitle)
                            .setContentText(notificationText)
                            .setAutoCancel(true)
                            .setTicker(notificationTitle)
                            .setStyle(new Notification.BigTextStyle()
                                    .bigText(notificationTextLong));
                } else {
                    notification = new Notification.Builder(BitCoinApp.getAppContext())
                            .setSmallIcon(R.drawable.ic_stat_equalizer)
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setContentTitle(notificationTitle)
                            .setContentText(notificationText)
                            .setAutoCancel(true)
                            .setTicker(notificationTitle)
                            .setVibrate(new long[]{100, 100, 300})
                            .setStyle(new Notification.BigTextStyle()
                                    .bigText(notificationTextLong));
                }
                try {
                    if (lineChartTask != null) {
                        Bitmap lineChart = lineChartTask.get();
                        if (lineChart != null) {
                            notification.setStyle(new Notification.BigPictureStyle()
                                    .bigPicture(lineChart)
                                    .bigLargeIcon((Bitmap) null)
                                    .setBigContentTitle(notificationTitleLong));
                            notification.setLargeIcon(lineChart);
                            ShareDataIntent dataIntent = new ShareDataIntent(localeContext,
                                    R.string.share_bitcoin_price_title);
                            if (dataIntent.saveImageToCache(lineChart)) {
                                Uri fileUri = dataIntent.getUriForSavedImage();
                                Uri googlePlayLink = Uri.parse(Constants.GOOGLE_PLAY_URL);
                                String bitCoinPriceChangedText = String.format(
                                        localeContext.getString(R.string.share_bitcoin_price),
                                        notificationText,
                                        MPU,
                                        googlePlayLink.toString());
                                PendingIntent shareIntent = dataIntent.shareImageWithText
                                        (bitCoinPriceChangedText, fileUri);
                                Notification.Action action = new Notification.Action(R.drawable.share,
                                        localeContext.getString(R.string.share), shareIntent);
                                notification.addAction(action);
                            }
                        }
                    }
                } catch (ExecutionException | InterruptedException e) {
                    Log.w(NTAG, "Unexpected exception while publishing " +
                            "notification", e);
                } finally {
                    notification.setContentIntent(clickIntent);
                    assert notificationManager != null;
                    notificationManager.notify(Constants.NOTIFICATION_ID, notification.build());
                }
            }
        } else
            Log.d(NTAG, Constants.LOG.NNOTIFYING);
    }

    private AsyncTask<String, Void, Bitmap> generateLineChart(Context localeContext) {
        Map<Date, Float> pricesMap;
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, -7);
        String startDate = String.format(Locale.US, "%d-%02d-%02d",
                start.get(Calendar.YEAR),
                (start.get(Calendar.MONTH) + 1),
                start.get(Calendar.DAY_OF_MONTH));
        String url = API_URL + "?start=" + startDate + "&end=" +
                new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());
        pricesMap = getValuesByDatedURL(url);
        if (pricesMap == null)
            return null;

        int divider = getDivider(pricesMap);
        Line bitcoinPricesLine = Plots.newLine(getNormalizedData(pricesMap), Color.DARKBLUE);
        bitcoinPricesLine.setLineStyle(LineStyle.MEDIUM_DOTTED_LINE);
        bitcoinPricesLine.addShapeMarkers(Shape.CIRCLE, Color.BLACK, 8);
        bitcoinPricesLine.setFillAreaColor(Color.CYAN);

        final LineChart bitcoinPricesChart = GCharts.newLineChart(bitcoinPricesLine);
        bitcoinPricesChart.setSize(760, 380);
        bitcoinPricesChart.setGrid(25, 25, 3, 2);
        bitcoinPricesChart.setTitle(localeContext.getString(R.string.chart_title));

        AxisStyle axisStyle = AxisStyle.newAxisStyle(Color.BLACK, 11, AxisTextAlignment.CENTER);
        AxisLabels xAxis = AxisLabelsFactory.newAxisLabels(getDates(pricesMap));
        AxisLabels yAxis = AxisLabelsFactory.newAxisLabels(getValues(pricesMap, divider));
        xAxis.setAxisStyle(axisStyle);
        yAxis.setAxisStyle(axisStyle);

        bitcoinPricesChart.addXAxisLabels(xAxis);
        bitcoinPricesChart.addYAxisLabels(yAxis);
        bitcoinPricesChart.setBackgroundFill(Fills.newSolidFill(Color.WHITE));
        ImageDownloader downloader = new ImageDownloader();
        return downloader.execute(bitcoinPricesChart.toURLString());
    }

    private int getDivider(@NonNull Map<Date, Float> pricesMap) {
        Float maximum = Collections.max(pricesMap.values());
        int iterations = 0;
        while (maximum >= 10) {
            maximum /= 10;
            ++iterations;
        }
        return (int) Math.pow(10, iterations);
    }

    private List<String> getDates(@NonNull Map<Date, Float> pricesMap) {
        List<String> dates = new ArrayList<>(pricesMap.keySet().size());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        for (Date currentDate : pricesMap.keySet())
            dates.add(formatter.format(currentDate));
        return dates;
    }

    private List<String> getValues(@NonNull Map<Date, Float> pricesMap, int divider) {
        Collection<Float> values = pricesMap.values();
        List<String> formattedValues = new ArrayList<>(6);
        float maximum = Collections.max(values);
        float minimum = Collections.min(values);
        float difference = maximum - minimum;
        float amount = difference / 5;
        formattedValues.add(0, String.format(Locale.US, "$%.2fK", (minimum / divider)));
        float latest = minimum;
        for (int i = 1; i < 5; ++i) {
            latest += amount;
            formattedValues.add(i, String.format(Locale.US, "$%.2fK", (latest / divider)));
        }
        formattedValues.add(5, String.format(Locale.US, "$%.2fK", (maximum / divider)));
        return formattedValues;
    }

    private Data getNormalizedData(@NonNull Map<Date, Float> pricesMap) {
        double[] normalizedValues = new double[pricesMap.values().size()];
        float maximum = Collections.max(pricesMap.values());
        float minimum = Collections.min(pricesMap.values());
        float difference = maximum - minimum;
        int i = 0;
        for (Float currentValue : pricesMap.values()) {
            float actualDifference = currentValue - minimum;
            normalizedValues[i] = (100 * actualDifference) / difference;
            ++i;
        }
        return Data.newData(normalizedValues);
    }

    private Map<Date, Float> getValuesByDatedURL(@NonNull String url) {
        net netRequest = new net();
        netRequest.execute(url);
        try {
            return JSONTools.sortDateByValue(
                    JSONTools.convert2DateHashMap(netRequest.get().getJSONObject("bpi")));
        } catch (InterruptedException | ExecutionException |
                JSONException | NullPointerException ignored) {
            return null;
        }
    }

    void updatePreferences() {
        updateSharedPreferences();
    }

    private static class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL httpImageUrl = new URL(urls[0]);
                URL httpsImageUrl = new URL("https", httpImageUrl.getHost(), httpImageUrl.getFile
                        ());
                Log.d(NTAG, "URL: " + httpsImageUrl);
                return BitmapFactory.decodeStream(httpsImageUrl.openConnection().getInputStream());
            } catch (IOException ex) {
                Log.w(NTAG, "Error while downloading the photo", ex);
                return null;
            }
        }

    }
}
