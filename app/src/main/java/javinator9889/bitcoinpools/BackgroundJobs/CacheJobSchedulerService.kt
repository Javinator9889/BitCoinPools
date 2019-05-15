package javinator9889.bitcoinpools.BackgroundJobs

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import javinator9889.bitcoinpools.BitCoinApp
import javinator9889.bitcoinpools.CacheManaging
import javinator9889.bitcoinpools.Constants
import javinator9889.bitcoinpools.JSONTools.JSONTools
import javinator9889.bitcoinpools.NetTools.net
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Javinator9889 on 04/03/2018.
 *
 *
 * Controls cache
 */

class CacheJobSchedulerService : JobService() {
    private var jobWorking = false
    private var jobCancelled = false

    private val jobHandler = Handler(Handler.Callback { msg ->
        if (!jobCancelled) {
            try {
                updateCache()
                jobWorking = false
                jobFinished(msg.obj as JobParameters, false)
                return@Callback false
            } catch (e: NullPointerException) {
                e.printStackTrace()
                jobWorking = false
                jobFinished(msg.obj as JobParameters, true)
                return@Callback false
            }

        }
        false
    })

    override fun onStartJob(params: JobParameters): Boolean {
        jobWorking = true
        jobCancelled = false
        jobHandler.sendMessage(Message.obtain(jobHandler, 2, params))
        Log.d("CACHEJOB", "Starting cache job")
        return jobWorking
    }

    override fun onStopJob(params: JobParameters): Boolean {
        jobCancelled = true
        val needsReschedule = jobWorking
        jobHandler.removeMessages(2)
        jobFinished(params, needsReschedule)
        return needsReschedule
    }

    private fun updateCache() {
        val date = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
                .format(Calendar.getInstance().time)
        val cache = CacheManaging.newInstance(BitCoinApp.getAppContext())
        try {
            cache.setupFile()
            val request = net()
            request.execute(Constants.STATS_URL)
            val valuesObtained = JSONTools.convert2HashMap(request.get())
            val newValuesToSave = LinkedHashMap<String, String>()
            newValuesToSave["date"] = date
            for (key in valuesObtained!!.keys) {
                newValuesToSave[key] = valuesObtained[key].toString()
            }
            cache.writeCache(newValuesToSave)
        } catch (e: Exception) {
            e.printStackTrace()
            throw NullPointerException("Impossible to obtain values")
        }

    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(BitCoinApp.localeManager.setLocale(base))
    }
}
