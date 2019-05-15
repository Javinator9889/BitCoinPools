package javinator9889.bitcoinpools

import android.annotation.SuppressLint
import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.metrics.AddTrace
import javinator9889.bitcoinpools.BackgroundJobs.CacheJobSchedulerService
import javinator9889.bitcoinpools.BackgroundJobs.JobSchedulerService
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.CACHE_JOB_PERIOD
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Javinator9889 on 22/01/2018. Based on: https://github.com/ZonaRMR/SimpleForFacebook/blob/master/app/src/main/java/com/creativetrends/simple/app/activities/SimpleApp.java
 */

class BitCoinApp : Application() {

    @AddTrace(name = "onCreateApplication")
    override fun onCreate() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        appContext = applicationContext
        try {
            CacheManaging.newInstance(this).setupFile()
        } catch (ignored: IOException) {
        }
        // This error should never happen
        startBackgroundJobs()
        super.onCreate()
        Log.d(Constants.LOG.BCTAG, Constants.LOG.CREATED_APP)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.START_DATE, Calendar.getInstance()
                .time.toString())
        firebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeManager.setLocale(this)
    }

    /**
     * Set the base context for this ContextWrapper.  All calls will then be delegated to the base
     * context.  Throws IllegalStateException if a base context has already been set.
     *
     * @param base The new base context for this wrapper.
     */
    override fun attachBaseContext(base: Context) {
        sharedPreferences = base.getSharedPreferences(
                Constants.SHARED_PREFERENCES.SHARED_PREFERENCES_KEY,
                Context.MODE_PRIVATE)
        initSharedPreferences(base)
        localeManager = LocaleManager()
        super.attachBaseContext(localeManager.setLocale(base))
    }

    private fun initSharedPreferences(base: Context) {
        val sharedPreferencesThread = Thread(Runnable {
            if (!sharedPreferences!!.contains(
                            Constants.SHARED_PREFERENCES.SHARED_PREFERENCES_INITIALIZED)) {
                Log.d(Constants.LOG.BCTAG, Constants.LOG.INIT_PREF)
                val sharedPreferencesEditor = sharedPreferences!!.edit()
                sharedPreferencesEditor.putBoolean(
                        Constants.SHARED_PREFERENCES.SHARED_PREFERENCES_INITIALIZED, true)
                sharedPreferencesEditor.putBoolean(
                        Constants.SHARED_PREFERENCES.NOTIFICATIONS_ENABLED, false)
                sharedPreferencesEditor.putBoolean(
                        Constants.SHARED_PREFERENCES.NOTIFIED_LOW, false)
                sharedPreferencesEditor.putBoolean(
                        Constants.SHARED_PREFERENCES.NOTIFIED_HIGH, false)
                sharedPreferencesEditor.putInt(
                        Constants.SHARED_PREFERENCES.DAYS_TO_CHECK, 1)
                sharedPreferencesEditor.putInt(
                        Constants.SHARED_PREFERENCES.VALUE_TO_CHECK, 1000)
                sharedPreferencesEditor.putString(
                        Constants.SHARED_PREFERENCES.APP_VERSION, appVersion(base))
                sharedPreferencesEditor.apply()
            }
        })
        sharedPreferencesThread.start()
    }

    companion object {
        var localeManager: LocaleManager
        @SuppressLint("StaticFieldLeak")
        var appContext: Context? = null
            private set
        var sharedPreferences: SharedPreferences? = null
            private set
        var firebaseAnalytics: FirebaseAnalytics? = null
            private set

        private fun startBackgroundJobs() {
            val backgroundJobsThread = Thread(Runnable {
                val mJobScheduler = appContext!!
                        .getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val builder = JobInfo.Builder(
                        1,
                        ComponentName(appContext!!.packageName,
                                JobSchedulerService::class.java.name))

                builder.setPeriodic(Constants.SCHEDULING_TIME)
                builder.setPersisted(Constants.PERSISTED)
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                builder.setBackoffCriteria(Constants.BACKOFF_CRITERIA,
                        JobInfo.BACKOFF_POLICY_LINEAR)

                assert(mJobScheduler != null)
                if (mJobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
                    Log.e(Constants.LOG.BCTAG,
                            Constants.LOG.NO_INIT + "JobScheduler"
                                    + mJobScheduler.allPendingJobs.toString())
                }

                if (isJobCreationNeeded(mJobScheduler)) {
                    val cacheBuilder = JobInfo.Builder(
                            2,
                            ComponentName(appContext!!.packageName,
                                    CacheJobSchedulerService::class.java.name))
                    val period = sharedPreferences!!.getInt(CACHE_JOB_PERIOD, 1)
                    cacheBuilder.setPeriodic(TimeUnit.DAYS.toMillis(period.toLong()))
                    cacheBuilder.setPersisted(Constants.PERSISTED)
                    cacheBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    cacheBuilder.setBackoffCriteria(Constants.BACKOFF_CRITERIA,
                            JobInfo.BACKOFF_POLICY_LINEAR)

                    if (mJobScheduler.schedule(cacheBuilder.build()) == JobScheduler
                                    .RESULT_FAILURE) {
                        Log.e(Constants.LOG.BCTAG,
                                Constants.LOG.NO_INIT + "JobScheduler" + mJobScheduler
                                        .allPendingJobs.toString())
                    } else {
                        val newValueForStartedJob = sharedPreferences!!.edit()
                        newValueForStartedJob.putBoolean(Constants.SHARED_PREFERENCES.CACHE_JOB,
                                true)
                        newValueForStartedJob.apply()
                    }
                }
            })
            backgroundJobsThread.start()
        }

        fun forceRestartBackgroundJobs() {
            Log.d(Constants.LOG.BCTAG, Constants.LOG.RESTART_JOB)
            val jobScheduler = appContext!!
                    .getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancelAll()
            startBackgroundJobs()
        }

        val isOnline: Boolean
            get() {
                val connectionManager = appContext!!
                        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val netInfo = connectionManager.activeNetworkInfo
                return netInfo != null && netInfo.isConnected
            }

        fun appVersion(base: Context): String {
            try {
                val pInfo = base.packageManager
                        .getPackageInfo(base.packageName, 0)
                return pInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                Crashlytics.logException(e)
                return "1.0"
            }

        }

        fun isJobCreationNeeded(applicationJobScheduler: JobScheduler?): Boolean {
            if (sharedPreferences!!.contains(Constants.SHARED_PREFERENCES.CACHE_JOB)) {
                val pendingJobs = applicationJobScheduler!!.allPendingJobs
                val currentPendingJob = pendingJobs.iterator()
                var equals = false
                while (currentPendingJob.hasNext() && !equals) {
                    val pending = currentPendingJob.next()
                    equals = pending.toString() == Constants.JOBINFO
                }
                val timeDiff = timeDifference()
                val period = sharedPreferences!!.getInt(CACHE_JOB_PERIOD, 1)
                return timeDiff >= TimeUnit.DAYS.toMillis(period.toLong()) && !equals
            } else {
                val newEntry = sharedPreferences!!.edit()
                newEntry.putBoolean(Constants.SHARED_PREFERENCES.CACHE_JOB, false)
                newEntry.apply()
                return true
            }
        }

        private fun timeDifference(): Long {
            val cache = CacheManaging.newInstance(appContext)
            try {
                val cachedValues = cache.readCache()
                val date = cachedValues!!["date"]
                val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
                val dateInCache = format.parse(date)
                return Calendar.getInstance().time.time - dateInCache.time
            } catch (e: Exception) {
                return java.lang.Long.MAX_VALUE
            }

        }
    }
}
