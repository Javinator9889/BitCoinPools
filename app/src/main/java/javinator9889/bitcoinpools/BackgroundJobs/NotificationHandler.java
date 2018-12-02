package javinator9889.bitcoinpools.BackgroundJobs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Build;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import javinator9889.bitcoinpools.BitCoinApp;
import javinator9889.bitcoinpools.Constants;
import javinator9889.bitcoinpools.DataLoaderScreen;
import javinator9889.bitcoinpools.FragmentViews.CustomMarkerView;
import javinator9889.bitcoinpools.JSONTools.JSONTools;
import javinator9889.bitcoinpools.MainActivity;
import javinator9889.bitcoinpools.NetTools.net;
import javinator9889.bitcoinpools.R;

import static javinator9889.bitcoinpools.Constants.API_URL;

/**
 * Created by Javinator9889 on 23/01/2018.
 * Based on: https://stackoverflow.com/a/46991229
 */

class NotificationHandler {
    private static boolean NOTIFICATIONS_ENABLED = false;
    private static boolean NOTIFIED_HIGH = false;
    private static boolean NOTIFIED_LOW = false;
    private static int SPECIFIC_VALUE = 0;
    private static float MPU;
    private Context mContext;

    private NotificationHandler(@NonNull Context context) {
        final SharedPreferences sp = BitCoinApp.getSharedPreferences();
        mContext = context;
        Log.d(Constants.LOG.NTAG, Constants.LOG.CREATING_NOTIFICATION);
        NOTIFICATIONS_ENABLED = sp.getBoolean(Constants.SHARED_PREFERENCES.NOTIFICATIONS_ENABLED,
                false);
        NOTIFIED_HIGH = sp.getBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_HIGH, false);
        NOTIFIED_LOW = sp.getBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_LOW, false);
        SPECIFIC_VALUE = sp.getInt(Constants.SHARED_PREFERENCES.VALUE_TO_CHECK, 1000);
        MPU = initMPU();
        Log.d(Constants.LOG.NTAG, Constants.LOG.CURRRENT_NOT_SETTINGS
        + NOTIFICATIONS_ENABLED + "\n"
        + NOTIFIED_HIGH + "\n"
        + NOTIFIED_LOW + "\n"
        + SPECIFIC_VALUE + "\n"
        + MPU);
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

        if (NOTIFICATIONS_ENABLED) {
            if ((MPU < SPECIFIC_VALUE) && !NOTIFIED_LOW) {
                notificationTitle = BitCoinApp.getAppContext().getString(R.string.lowerPrice);
                notificationTextLong = BitCoinApp.getAppContext().getString(R.string.lowerPriceX)
                        + SPECIFIC_VALUE + ". "
                        + BitCoinApp.getAppContext().getString(R.string.actualCost) + MPU;
                notificationText = BitCoinApp.getAppContext().getString(R.string.lowerPriceX)
                        + SPECIFIC_VALUE;
                NOTIFIED_HIGH = false;
                NOTIFIED_LOW = true;
                notify = (MPU != -1);
            } else if ((MPU > SPECIFIC_VALUE) && !NOTIFIED_HIGH) {
                notificationTitle = BitCoinApp.getAppContext().getString(R.string.morePrice);
                notificationTextLong = BitCoinApp.getAppContext().getString(R.string.morePriceX)
                        + SPECIFIC_VALUE + ". " + BitCoinApp.getAppContext().getString(R.string.actualCost) + MPU;
                notificationText = BitCoinApp.getAppContext().getString(R.string.morePriceX)
                        + SPECIFIC_VALUE;
                NOTIFIED_HIGH = true;
                NOTIFIED_LOW = false;
                notify = (MPU != -1);
            }
            if (notify) {
                Log.d(Constants.LOG.NTAG, Constants.LOG.NOTIFYING);
                updatePreferences();
                final LineChart chart = new LineChart(mContext);
                Bitmap chartBitmap = generateLineChart(chart);
                String name = BitCoinApp.getAppContext().getString(R.string.alerts);
                String description = BitCoinApp.getAppContext().getString(R.string.description);
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
                            .setStyle(new Notification.BigTextStyle()
                                    .bigText(notificationTextLong));
                }
                if (chartBitmap != null) {
                    notification.setLargeIcon(chartBitmap);
                    notification.setStyle(new Notification.BigPictureStyle()
                            .bigPicture(chartBitmap)
                            .bigLargeIcon((Bitmap) null));
                }
                notification.setContentIntent(clickIntent);
                assert notificationManager != null;
                notificationManager.notify(Constants.NOTIFICATION_ID, notification.build());
            } else
                Log.d(Constants.LOG.NTAG, Constants.LOG.NNOTIFYING);
        }
    }

    private Bitmap generateLineChart(@NonNull final LineChart lineChart) {
        Map<Date, Float> pricesMap;
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DAY_OF_MONTH, -7);
        String startDate = String.format(Locale.US, "%d-%02d-%02d",
                start.get(Calendar.YEAR),
                start.get(Calendar.MONTH),
                start.get(Calendar.DAY_OF_MONTH));
        String url = API_URL + "?start=" + startDate + "&end=" +
                new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());
        pricesMap = getValuesByDatedURL(url);
        if (pricesMap == null)
            return null;
//        lineChart.setDrawingCacheEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        CustomMarkerView markerView = new CustomMarkerView(mContext, R.layout.marker_view);
        markerView.setChartView(lineChart);
        lineChart.setMarker(markerView);
        ArrayList<Entry> values = new ArrayList<>(pricesMap.size());
        int i = 0;
        for (Date currentDate : pricesMap.keySet()) {
            values.add(new Entry(i, pricesMap.get(currentDate)));
            ++i;
        }
        LineDataSet lineDataSet = new LineDataSet(values, mContext.getString(R.string
                .latest_7_days));
        lineDataSet.setDrawIcons(false);
        lineDataSet.enableDashedLine(10f, 5f, 0f);
        lineDataSet.enableDashedHighlightLine(10f, 5f, 0f);
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(9f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        lineDataSet.setFormSize(15.f);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setFillDrawable(ContextCompat.getDrawable(mContext, R.drawable.fade_red));
        lineDataSet.setDrawCircles(false);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>(1);
        dataSets.add(lineDataSet);
        LineData data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.getAxisLeft().setValueFormatter(new LargeValueFormatter());
        return setLayoutParams(lineChart);
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

    private Bitmap setLayoutParams(@NonNull final LineChart lineChart) {
        float dpHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        float dpWidth = mContext.getResources().getDisplayMetrics().widthPixels;

        int[] attrs = new int[]{R.attr.actionBarSize};
        TypedArray array = mContext.obtainStyledAttributes(attrs);
        int size = array.getDimensionPixelSize(0, 0);
        array.recycle();

        int finalHeightDp = (int) ((dpHeight - size) * 0.5);
        int finalWidthDp = (int) ((dpWidth - size) * 0.9);

        System.out.println("Height: " + finalHeightDp);
        System.out.println("Width: " + finalWidthDp);

        Bitmap bitmap = Bitmap.createBitmap(finalWidthDp, finalHeightDp, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        ConstraintLayout.LayoutParams layoutParams =
                new ConstraintLayout.LayoutParams(finalWidthDp, finalHeightDp);
        layoutParams.matchConstraintMaxHeight = (int) dpHeight;
        layoutParams.matchConstraintMaxWidth = (int) dpWidth;
        layoutParams.orientation = ConstraintLayout.LayoutParams.HORIZONTAL;
        layoutParams.validate();

        lineChart.setLayoutParams(layoutParams);
        lineChart.invalidate();
        lineChart.draw(canvas);
        return bitmap;
    }

    @NonNull
    static NotificationHandler newInstance(@NonNull Context context) {
        return new NotificationHandler(context);
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

    void updatePreferences() {
        updateSharedPreferences();
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
}
