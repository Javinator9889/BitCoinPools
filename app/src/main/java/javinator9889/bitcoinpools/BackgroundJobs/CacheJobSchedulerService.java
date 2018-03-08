package javinator9889.bitcoinpools.BackgroundJobs;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import javinator9889.bitcoinpools.BitCoinApp;
import javinator9889.bitcoinpools.CacheManaging;
import javinator9889.bitcoinpools.Constants;
import javinator9889.bitcoinpools.JSONTools.JSONTools;
import javinator9889.bitcoinpools.NetTools.net;

/**
 * Created by Javinator9889 on 04/03/2018.
 *
 * Controls cache
 */

public class CacheJobSchedulerService extends JobService {
    private boolean jobWorking = false;
    private boolean jobCancelled = false;

    private Handler jobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (!jobCancelled) {
                try {
                    updateCache();
                    jobWorking = false;
                    jobFinished((JobParameters) msg.obj, false);
                    return false;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    jobWorking = false;
                    jobFinished((JobParameters) msg.obj, true);
                    return false;
                }
            }
            return false;
        }
    });

    @Override
    public boolean onStartJob(JobParameters params) {
        jobWorking = true;
        jobCancelled = false;
        jobHandler.sendMessage(Message.obtain(jobHandler, 2, params));
        Log.d("CACHEJOB", "Starting cache job");
        return jobWorking;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobCancelled = true;
        boolean needsReschedule = jobWorking;
        jobHandler.removeMessages(2);
        jobFinished(params, needsReschedule);
        return needsReschedule;
    }

    private void updateCache() {
        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
                .format(Calendar.getInstance().getTime());
        CacheManaging cache = CacheManaging.newInstance(BitCoinApp.getAppContext());
        try {
            cache.setupFile();
            net request = new net();
            request.execute(Constants.STATS_URL);
            HashMap<String, Float> valuesObtained = JSONTools.convert2HashMap(request.get());
            HashMap<String, String> newValuesToSave = new LinkedHashMap<>();
            newValuesToSave.put("date", date);
            for (String key : valuesObtained.keySet()) {
                newValuesToSave.put(key, String.valueOf(valuesObtained.get(key)));
            }
            cache.writeCache(newValuesToSave);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException("Impossible to obtain values");
        }
    }
}
