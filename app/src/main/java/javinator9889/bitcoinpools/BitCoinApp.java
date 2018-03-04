package javinator9889.bitcoinpools;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javinator9889.bitcoinpools.BackgroundJobs.CacheJobSchedulerService;
import javinator9889.bitcoinpools.BackgroundJobs.JobSchedulerService;

/**
 * Created by Javinator9889 on 22/01/2018.
 * Based on: https://github.com/ZonaRMR/SimpleForFacebook/blob/master/app/src/main/java/com/creativetrends/simple/app/activities/SimpleApp.java
 */

public class BitCoinApp extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Context APPLICATION_CONTEXT;
    private static SharedPreferences SHARED_PREFERENCES;

    public static Context getAppContext() {
        return APPLICATION_CONTEXT;
    }

    public static SharedPreferences getSharedPreferences() {
        return SHARED_PREFERENCES;
    }

    @Override
    public void onCreate() {
        APPLICATION_CONTEXT = getApplicationContext();
        SHARED_PREFERENCES = getSharedPreferences(Constants.SHARED_PREFERENCES.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        initSharedPreferences();
        startBackgroundJobs();
        try {
            System.out.println(CacheManaging.newInstance(this).readCache());
        } catch (Exception e) {
            System.out.println("No cache values found");
        }
        super.onCreate();
        Log.d(Constants.LOG.BCTAG, Constants.LOG.CREATED_APP);
    }

    private static void startBackgroundJobs() {
        JobScheduler globalJobScheduler = (JobScheduler) APPLICATION_CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> pendingJobs = globalJobScheduler != null ? globalJobScheduler.getAllPendingJobs() : new ArrayList<JobInfo>(0);

        JobScheduler mJobScheduler = (JobScheduler) APPLICATION_CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(APPLICATION_CONTEXT.getPackageName(), JobSchedulerService.class.getName()));

        builder.setPeriodic(Constants.SCHEDULING_TIME);
        builder.setPersisted(Constants.PERSISTED);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setBackoffCriteria(Constants.BACKOFF_CRITERIA, JobInfo.BACKOFF_POLICY_LINEAR);

        assert mJobScheduler != null;
        if (mJobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
            Log.e(Constants.LOG.BCTAG, Constants.LOG.NO_INIT + "JobScheduler" + mJobScheduler.getAllPendingJobs().toString());
        }

        JobScheduler cacheJobScheduler = (JobScheduler) APPLICATION_CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder cacheBuilder = new JobInfo.Builder(2, new ComponentName(APPLICATION_CONTEXT.getPackageName(), CacheJobSchedulerService.class.getName()));


        cacheBuilder.setPeriodic(TimeUnit.DAYS.toMillis(1));
        cacheBuilder.setPersisted(Constants.PERSISTED);
        cacheBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        cacheBuilder.setBackoffCriteria(Constants.BACKOFF_CRITERIA, JobInfo.BACKOFF_POLICY_LINEAR);

        if ((pendingJobs.size() != 0) && !(pendingJobs.contains(cacheBuilder.build()))) {
            assert cacheJobScheduler != null;
            if (cacheJobScheduler.schedule(cacheBuilder.build()) == JobScheduler.RESULT_FAILURE) {
                Log.e(Constants.LOG.BCTAG, Constants.LOG.NO_INIT + "JobScheduler" + mJobScheduler.getAllPendingJobs().toString());
            }
        } else
            System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE EL JOB YA EXISTE");
    }

    private void initSharedPreferences() {
        if (!SHARED_PREFERENCES.contains(Constants.SHARED_PREFERENCES.SHARED_PREFERENCES_INITIALIZED)) {
            Log.d(Constants.LOG.BCTAG, Constants.LOG.INIT_PREF);
            SharedPreferences.Editor sharedPreferencesEditor = SHARED_PREFERENCES.edit();
            sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES.SHARED_PREFERENCES_INITIALIZED, true);
            sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES.NOTIFICATIONS_ENABLED, false);
            sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_LOW, false);
            sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES.NOTIFIED_HIGH, false);
            sharedPreferencesEditor.putInt(Constants.SHARED_PREFERENCES.DAYS_TO_CHECK, 1);
            sharedPreferencesEditor.putInt(Constants.SHARED_PREFERENCES.VALUE_TO_CHECK, 1000);
            sharedPreferencesEditor.putString(Constants.SHARED_PREFERENCES.APP_VERSION, appVersion());
            sharedPreferencesEditor.apply();
        }
    }

    public static void forceRestartBackgroundJobs() {
        Log.d(Constants.LOG.BCTAG, Constants.LOG.RESTART_JOB);
        JobScheduler jobScheduler = (JobScheduler) APPLICATION_CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert jobScheduler != null;
        jobScheduler.cancelAll();
        startBackgroundJobs();
    }

    public static boolean isOnline() {
        ConnectivityManager connectionManager = (ConnectivityManager) getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectionManager != null;
        NetworkInfo netInfo = connectionManager.getActiveNetworkInfo();
        return ((netInfo != null) && netInfo.isConnected());
    }

    public static String appVersion() {
        try {
            PackageInfo pInfo = getAppContext().getPackageManager().getPackageInfo(getAppContext().getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Crashlytics.logException(e);
            return "1.0";
        }
    }
}
