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
    private boolean jobWorking = false;
    private boolean jobCancelled = false;

    private Handler jobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (!jobCancelled) {
                notificationHandler.putNotification();
                Log.d(Constants.LOG.JTAG, Constants.LOG.RECEIVED_JOB);
                jobWorking = false;
                jobFinished((JobParameters) msg.obj, false);
                notificationHandler.updatePreferences();
                return false;
            }
            return false;
        }
    });

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(Constants.LOG.JTAG, Constants.LOG.STARTING_JOB + Constants.JOB_ID);
        jobWorking = true;
        notificationHandler = NotificationHandler.newInstance(this);
        jobHandler.sendMessage(Message.obtain(jobHandler, Constants.JOB_ID, params));
        return jobWorking;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(Constants.LOG.JTAG, Constants.LOG.STOPPING_JOB + Constants.JOB_ID);
        jobCancelled = true;
        boolean needsReschedule = jobWorking;
        jobHandler.removeMessages(Constants.JOB_ID);
        jobFinished(params, needsReschedule);
        return needsReschedule;
    }
}
