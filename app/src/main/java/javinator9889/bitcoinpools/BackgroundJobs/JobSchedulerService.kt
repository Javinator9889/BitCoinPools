package javinator9889.bitcoinpools.BackgroundJobs

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log

import javinator9889.bitcoinpools.BitCoinApp
import javinator9889.bitcoinpools.Constants


/**
 * Created by Javinator9889 on 23/01/2018. Based on: https://code.tutsplus.com/tutorials/using-the-jobscheduler-api-on-android-lollipop--cms-23562
 */

class JobSchedulerService : JobService() {
    private var notificationHandler: NotificationHandler? = null
    private var jobWorking = false
    private var jobCancelled = false

    private val jobHandler = Handler(Handler.Callback { msg ->
        if (!jobCancelled) {
            notificationHandler!!.putNotification()
            Log.d(Constants.LOG.JTAG, Constants.LOG.RECEIVED_JOB)
            jobWorking = false
            jobFinished(msg.obj as JobParameters, false)
            notificationHandler!!.updatePreferences()
            return@Callback false
        }
        false
    })

    override fun onStartJob(params: JobParameters): Boolean {
        Log.d(Constants.LOG.JTAG, Constants.LOG.STARTING_JOB + Constants.JOB_ID)
        jobWorking = true
        notificationHandler = NotificationHandler.newInstance()
        jobHandler.sendMessage(Message.obtain(jobHandler, Constants.JOB_ID, params))
        return jobWorking
    }

    override fun onStopJob(params: JobParameters): Boolean {
        Log.d(Constants.LOG.JTAG, Constants.LOG.STOPPING_JOB + Constants.JOB_ID)
        jobCancelled = true
        val needsReschedule = jobWorking
        jobHandler.removeMessages(Constants.JOB_ID)
        jobFinished(params, needsReschedule)
        return needsReschedule
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(BitCoinApp.localeManager.setLocale(base))
    }
}
