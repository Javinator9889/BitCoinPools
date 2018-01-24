package javinator9889.bitcoinpools.BackgroundJobs;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import javinator9889.bitcoinpools.Constants;


/**
 * Created by Javinator9889 on 23/01/2018.
 * Based on: https://code.tutsplus.com/tutorials/using-the-jobscheduler-api-on-android-lollipop--cms-23562
 */

public class JobSchedulerService extends JobService {
    private NotificationHandler notificationHandler;

    private Handler jobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            notificationHandler.putNotification();
            Log.d(Constants.LOG.JTAG, Constants.LOG.RECEIVED_JOB);
            jobFinished((JobParameters) msg.obj, false);
            notificationHandler.updatePreferences();
            return false;
        }
    });

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(Constants.LOG.JTAG, Constants.LOG.STARTING_JOB);
        notificationHandler = NotificationHandler.newInstance();
        jobHandler.sendMessage(Message.obtain(jobHandler, Constants.JOB_ID, params));
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(Constants.LOG.JTAG, Constants.LOG.STOPPING_JOB);
        jobHandler.removeMessages(Constants.JOB_ID);
        return false;
    }
}
