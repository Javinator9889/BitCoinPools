package javinator9889.bitcoinpools.AppUpdaterManager

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import javinator9889.bitcoinpools.BitCoinApp
import javinator9889.bitcoinpools.Constants
import org.json.JSONException
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Created by Javinator9889 on 24/01/2018. Check for updates based on GitHub releases
 */

class CheckUpdates(GitHub_User: String, GitHub_Repo: String) {

    init {
        CONNECTION_URL = "$GITHUB_API_URL$GitHub_User/$GitHub_Repo/releases"
        try {
            APP_VERSION = BitCoinApp.getAppContext().getPackageManager()
                    .getPackageInfo(BitCoinApp.getAppContext().getPackageName(), 0)
                    .versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(Constants.LOG.CTAG, Constants.LOG.NO_INFO + e.message,
                    PackageManager.NameNotFoundException())
        }

        getData()
    }

    fun checkForUpdates(dialogContext: Context, title: String, description: String,
                        positiveText: String, negativeText: String, neutralText: String) {
        if (isAnyVersionAvailable(LATEST_VERSION, APP_VERSION)) {
            Log.d(Constants.LOG.CTAG, Constants.LOG.NEW_VERSION + APP_VERSION
                    + " | " + LATEST_VERSION)
            val materialDialog: MaterialDialog
            if (!HTML_PAGE) {
                Log.d(Constants.LOG.CTAG, Constants.LOG.DOW_NOTIFICATION)
                materialDialog = MaterialDialog.Builder(dialogContext)
                        .title(title)
                        .content(description)
                        .negativeText(negativeText)
                        .neutralText(positiveText)
                        .cancelable(false)
                        .onAny { dialog, which ->
                            when (which) {
                                DialogAction.NEUTRAL -> try {
                                    val webUri = Uri.parse(
                                            "market://details?id=javinator9889.bitcoinpools")
                                    val launchBrowser = Intent(Intent.ACTION_VIEW,
                                            webUri)
                                    dialogContext.startActivity(launchBrowser)
                                } catch (e: ActivityNotFoundException) {
                                    val webUri = Uri.parse(MORE_INFO)
                                    val launchBrowser = Intent(Intent.ACTION_VIEW,
                                            webUri)
                                    dialogContext.startActivity(launchBrowser)
                                }

                                DialogAction.NEGATIVE -> dialog.dismiss()
                                else -> {
                                }
                            }
                        }
                        .build()
            } else {
                Log.d(Constants.LOG.CTAG, Constants.LOG.NDOW_NOTIFICATION)
                materialDialog = MaterialDialog.Builder(dialogContext)
                        .title(title)
                        .content(description)
                        .positiveText(neutralText)
                        .negativeText(negativeText)
                        .cancelable(false)
                        .onPositive { dialog, which ->
                            val webUri = Uri.parse(MORE_INFO)
                            val launchBrowser = Intent(Intent.ACTION_VIEW, webUri)
                            dialogContext.startActivity(launchBrowser)
                        }
                        .onNegative { dialog, which -> dialog.dismiss() }
                        .build()
            }
            materialDialog.show()
        }
    }

    private fun getData() {
        val connection = NetworkConnection()
        connection.execute(CONNECTION_URL)
        try {
            val RETRIEVED_DATA = connection.get()
            val jsonLength = RETRIEVED_DATA.length()
            var noPreRelease = false
            var i = 0
            while (i < jsonLength && !noPreRelease) {
                if (!RETRIEVED_DATA.getJSONObject(i).getBoolean("prerelease")) {
                    noPreRelease = true
                    LATEST_VERSION = RETRIEVED_DATA.getJSONObject(i).getString("tag_name")
                    var apkFound = false
                    var j = 0
                    while (j < RETRIEVED_DATA.getJSONObject(i).getJSONArray("assets")
                                    .length() && !apkFound) {
                        if (!(RETRIEVED_DATA.getJSONObject(i).getJSONArray("assets").isNull(j) && RETRIEVED_DATA.getJSONObject(i).getJSONArray("assets")
                                        .getJSONObject(j).getString("name").contains(".apk"))) {
                            apkFound = true
                            DOWNLOAD_URL = RETRIEVED_DATA.getJSONObject(i)
                                    .getJSONArray("assets").getJSONObject(0)
                                    .getString("browser_download_url")
                        }
                        ++j
                    }
                    if (DOWNLOAD_URL == null) {
                        HTML_PAGE = true
                    }
                }
                ++i
            }
        } catch (e: InterruptedException) {
            Crashlytics.logException(e)
            Log.e(Constants.LOG.CTAG, Constants.LOG.NO_INFO + e.message)
        } catch (e: ExecutionException) {
            Crashlytics.logException(e)
            Log.e(Constants.LOG.CTAG, Constants.LOG.NO_INFO + e.message)
        } catch (e: JSONException) {
            Crashlytics.logException(e)
            Log.e(Constants.LOG.CTAG, Constants.LOG.NO_INFO + e.message)
        } catch (e: NullPointerException) {
            Crashlytics.logException(e)
            Log.e(Constants.LOG.CTAG, Constants.LOG.NO_INFO + e.message)
        }

    }

    fun isAnyVersionAvailable(latestVersion: String?, appVersion: String?): Boolean {
        Scanner(latestVersion).use { s1 ->
            Scanner(appVersion).use { s2 ->
                s1.useDelimiter("\\.")
                s2.useDelimiter("\\.")

                while (s1.hasNextInt() && s2.hasNextInt()) {
                    val v1 = s1.nextInt()
                    val v2 = s2.nextInt()
                    if (v1 < v2) {
                        return false
                    } else if (v1 > v2) {
                        return true
                    }
                }

                if (s1.hasNextInt() && s1.nextInt() != 0)
                    return true //latestVersion has an additional lower-level version number
                return if (s2.hasNextInt() && s2.nextInt() != 0) false else false //appVersion has an additional lower-level version
            }
        } // end of try-with-resources
    }

    companion object {
        private val GITHUB_API_URL = "https://api.github.com/repos/"
        private var CONNECTION_URL: String
        private var APP_VERSION: String? = null
        private var LATEST_VERSION: String? = null
        private var DOWNLOAD_URL: String? = null
        private val MORE_INFO = Constants.GOOGLE_PLAY_URL
        private var HTML_PAGE = false
    }
}
