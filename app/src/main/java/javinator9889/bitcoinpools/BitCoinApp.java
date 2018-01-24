package javinator9889.bitcoinpools;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import javinator9889.bitcoinpools.BackgroundJobs.JobSchedulerService;

/**
 * Created by Javinator9889 on 22/01/2018.
 * Based on: https://github.com/ZonaRMR/SimpleForFacebook/blob/master/app/src/main/java/com/creativetrends/simple/app/activities/SimpleApp.java
 */
@ReportsCrashes(
        mailTo = "javialonso007@hotmail.es",
        mode = ReportingInteractionMode.DIALOG,
        resDialogText = R.string.crash,
        logcatArguments = {"-t", "200", "-v", "long"}
)

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
        super.onCreate();
        ACRA.init(this);
        //ACRA.getErrorReporter().handleException(null);
        Log.d(Constants.LOG.BCTAG, Constants.LOG.CREATED_APP);
    }

    private static void startBackgroundJobs() {
        JobScheduler mJobScheduler = (JobScheduler) APPLICATION_CONTEXT.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(APPLICATION_CONTEXT.getPackageName(), JobSchedulerService.class.getName()));

        builder.setPeriodic(Constants.SCHEDULING_TIME);
        builder.setPersisted(Constants.PERSISTED);

        assert mJobScheduler != null;
        if (mJobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
            Log.e(Constants.LOG.BCTAG, Constants.LOG.NO_INIT + "JobScheduler" + mJobScheduler.getAllPendingJobs().toString());
        }
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
}
