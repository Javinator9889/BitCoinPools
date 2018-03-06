package javinator9889.bitcoinpools.BackgroundJobs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

import javinator9889.bitcoinpools.BitCoinApp;
import javinator9889.bitcoinpools.Constants;
import javinator9889.bitcoinpools.MainActivity;
import javinator9889.bitcoinpools.NetTools.net;
import javinator9889.bitcoinpools.R;

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

    private NotificationHandler() {
        final SharedPreferences sp = BitCoinApp.getSharedPreferences();
        Log.d(Constants.LOG.NTAG, Constants.LOG.CREATING_NOTIFICATION);
        NOTIFICATIONS_ENABLED = sp.getBoolean(Constants.SHARED_PREFERENCES.NOTIFICATIONS_ENABLED, false);
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
        NotificationManager notificationManager = (NotificationManager) BitCoinApp.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent clickIntent = PendingIntent.getActivity(
                BitCoinApp.getAppContext(),
                Constants.REQUEST_CODE,
                new Intent(BitCoinApp.getAppContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        if (NOTIFICATIONS_ENABLED) {
            if ((MPU < SPECIFIC_VALUE) && !NOTIFIED_LOW) {
                notificationTitle = BitCoinApp.getAppContext().getString(R.string.lowerPrice);
                notificationTextLong = BitCoinApp.getAppContext().getString(R.string.lowerPriceX) + SPECIFIC_VALUE + ". " + BitCoinApp.getAppContext().getString(R.string.actualCost) + MPU;
                notificationText = BitCoinApp.getAppContext().getString(R.string.lowerPriceX) + SPECIFIC_VALUE;
                NOTIFIED_HIGH = false;
                NOTIFIED_LOW = true;
                //notify = true;
                notify = (MPU != -1);
            } else if ((MPU > SPECIFIC_VALUE) && !NOTIFIED_HIGH) {
                notificationTitle = BitCoinApp.getAppContext().getString(R.string.morePrice);
                notificationTextLong = BitCoinApp.getAppContext().getString(R.string.morePriceX) + SPECIFIC_VALUE + ". " + BitCoinApp.getAppContext().getString(R.string.actualCost) + MPU;
                notificationText = BitCoinApp.getAppContext().getString(R.string.morePriceX) + SPECIFIC_VALUE;
                NOTIFIED_HIGH = true;
                NOTIFIED_LOW = false;
                //notify = true;
                notify = (MPU != -1);
            }
            if (notify) {
                Log.d(Constants.LOG.NTAG, Constants.LOG.NOTIFYING);
                String name = BitCoinApp.getAppContext().getString(R.string.alerts);
                String description = BitCoinApp.getAppContext().getString(R.string.description);
                Notification.Builder notification;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
                    assert notificationManager != null;
                    NotificationChannel mChannel = notificationManager.getNotificationChannel(Constants.CHANNEL_ID);
                    if (mChannel == null) {
                        mChannel = new NotificationChannel(Constants.CHANNEL_ID, name, importance);
                        mChannel.setDescription(description);
                        mChannel.enableVibration(true);
                        mChannel.setVibrationPattern(new long[]{100, 100, 300});
                        notificationManager.createNotificationChannel(mChannel);
                    }

                    notification = new Notification.Builder(BitCoinApp.getAppContext(), Constants.CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_stat_equalizer)
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setContentTitle(notificationTitle)
                            .setContentText(notificationText)
                            .setAutoCancel(true)
                            .setTicker(notificationTitle)
                            .setStyle(new Notification.BigTextStyle().bigText(notificationTextLong));
                } else {
                    notification = new Notification.Builder(BitCoinApp.getAppContext())
                            .setSmallIcon(R.drawable.ic_stat_equalizer)
                            .setCategory(Notification.CATEGORY_MESSAGE)
                            .setContentTitle(notificationTitle)
                            .setContentText(notificationText)
                            .setAutoCancel(true)
                            .setTicker(notificationTitle)
                            .setStyle(new Notification.BigTextStyle().bigText(notificationTextLong));
                }
                notification.setContentIntent(clickIntent);
                assert notificationManager != null;
                notificationManager.notify(Constants.NOTIFICATION_ID, notification.build());
            } else
                Log.d(Constants.LOG.NTAG, Constants.LOG.NNOTIFYING);
        }
    }

    static NotificationHandler newInstance() {
        return new NotificationHandler();
    }

    private static float initMPU() {
        net market = new net();
        market.execute("https://api.blockchain.info/stats");
        try {
            return MainActivity.round((float) market.get().getDouble("market_price_usd"), 2);
        } catch (InterruptedException | ExecutionException | JSONException | NullPointerException e) {
            return -1;
        }
    }

    void updatePreferences() {
        updateSharedPreferences();
    }

    private static void updateSharedPreferences() {
        final SharedPreferences.Editor sharedPreferencesEditor = BitCoinApp.getSharedPreferences().edit();
        sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_HIGH, NOTIFIED_HIGH);
        sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_LOW, NOTIFIED_LOW);
        sharedPreferencesEditor.apply();
    }
}
