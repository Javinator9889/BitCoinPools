package javinator9889.bitcoinpools

import java.util.concurrent.TimeUnit

/**
 * Created by Javinator9889 on 23/01/2018. Contains constant values that will be used
 */

object Constants {
    val PERSISTED = true
    val SCHEDULING_TIME = TimeUnit.HOURS.toMillis(1)
    val BACKOFF_CRITERIA = TimeUnit.SECONDS.toMillis(30)
    val JOB_ID = 1
    val JOBINFO = "(job:2/javinator9889.bitcoinpools/.BackgroundJobs.CacheJobSchedulerService)"
    val MILLIS_A_DAY: Long = 86400000
    val GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=javinator9889.bitcoinpools"
    val API_URL = "https://api.coindesk.com/v1/bpi/historical/close.json"
    val CHANNEL_ID = "javinator9889.bitcoinpools.Alerts"
    val NOTIFICATION_ID = 1
    val REQUEST_CODE = 0
    val GITHUB_USER = "Javinator9889"
    val GITHUB_REPO = "BitCoinPools"
    val STATS_URL = "https://api.blockchain.info/stats"
    val MARKET_NAME = "market_price_usd"
    val POOLS_URL = "https://api.blockchain.info/pools?timespan="
    val TRANSLATION_URL = "https://www.javinator9889.com/bitcoinpools-translation/"

    object PAYMENTS {
        val GOOGLE_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgt71diQE3dVAJ/KEJSpt+ZIEeeKDOWl5cBdwRirkjiVPtzwbOlOyJsf+tJzQrYvxJejmkfdwR5TlG4Z+NAZDtcS4mq63JVoPyEbmx0wvVYC3+zav2MbJO9P/gSmwTK0KGwVSyItcH5sqXjK9Mv280uj2jM0IMW0UpM91vzeitCGCbJRwMe1CnzLzFPFI01YJ/QjG+1KY7MzIhn3P2ZbS9C7fhP0BwJIBPoZJkp64pKhXf7iI5qsbZGby4V+iQiU5ONiS+ggy8X076IAB1DijL90BUnbTXCwa1WufChb3da7xV/AiPEHl9UJ2J70I3+/1Dx9MXOrYkBmOKAYFLJlcQwIDAQAB"
        val GOOGLE_CATALOG = arrayOf("in_app_purchases_donations", "in_app_purchases_donations_2", "in_app_purchases_donations_3", "in_app_purchases_donations_5", "in_app_purchases_donations_8", "in_app_purchases_donations_13")
        val PAYPALME = "https://paypal.me/Javinator9889"
    }

    object SHARED_PREFERENCES {
        val SHARED_PREFERENCES_KEY = "javinator9889.bitcoinpools.usrPreferences"
        val NOTIFICATIONS_ENABLED = "notifications_enabled"
        val NOTIFIED_HIGH = "notified_high"
        val NOTIFIED_LOW = "notified_low"
        val DAYS_TO_CHECK = "days_to_check"
        val VALUE_TO_CHECK = "value_to_check"
        val SHARED_PREFERENCES_INITIALIZED = "initialized"
        val APP_VERSION = "APP_VERSION"
        val CACHE_JOB = "CACHE_JOB"
        val CACHE_JOB_PERIOD = "CACHE_JOB_PERIOD"
        val CUSTOM_PRICE = "userCustomPrice"
        val CUSTOM_POOLS = "userCustomNumberOfPools"
        val HAS_USER_DEFINED_CUSTOM_PRICE = "userDefinedCustomPrice"
        val USER_LANGUAGE = "userLanguage"
    }

    object LOG {
        val UNCAUGHT_ERROR = "Uncaught error on: "

        val BCTAG = "BitCoinApp"
        val NO_INIT = "Unable to init current activity: "
        val INIT_PREF = "Initialising user shared preferences"
        val RESTART_JOB = "Restarting background jobs..."
        val CREATED_APP = "Correctly created application"

        val LTAG = "License"
        val INIT_L = "Created license page"

        val MATAG = "MainActivity"
        val CREATING_MAINVIEW = "Creating application Main View"
        val CREATING_CHART = "Creating application chart"
        val INIT_VALUES = "Initialising application values"
        val LISTENING = "Listening to buttons"
        val LOADING_MPU = "Loading MPU in a new thread..."
        val LOADING_RD = "Loading data in a new thread..."
        val LOADING_CHART = "Loading PieChart in a new thread..."
        val LOADING_TABLE = "Loading table in a new thread..."
        val MARKET_PRICE_ERROR = "Error on MainActivity.initMPU(): "
        val DATA_ERROR = "Error on MainActivity.initRD(): "
        val JOIN_ERROR = "Failed to join thread "

        val STAG = "SpinnerActivity"
        val INIT_SETTINGS_VIEW = "Starting settings view..."
        val INIT_SPINNER = "Starting configurations of spinners based on user preferences"
        val INIT_SWITCH = "Configuring options for switch in settings activity. Current state: "
        val CHANGE_PREFERENCES = "Changing preferences on "
        val BACK_TO_MC = "Going back to MainActivity. Saving data..."

        val JTAG = ".JobSchedulerService"
        val RECEIVED_JOB = "Correctly received job"
        val STARTING_JOB = "Starting current job and notification handler. Job ID: "
        val STOPPING_JOB = "Stopping current job. The job was interrupted. Job ID: "

        val NTAG = ".NotificationHandler"
        val CREATING_NOTIFICATION = "Creating current notification"
        val CURRRENT_NOT_SETTINGS = "Current notification settings: (ENABLED, NOTIFIED_HIGH_ NOTIFIED_LOW, SPECIFIC_VALUE, MPU)"
        val NOTIFYING = "Notifying to user"
        val NNOTIFYING = "Not notifying to user"

        val CTAG = ".CheckUpdates"
        val NO_INFO = "The API was unable to get the package information. Full trace: "
        val NEW_VERSION = "There is a new version available. Versions: (current | new) "
        val DOW_NOTIFICATION = "Creating a notification with download button"
        val NDOW_NOTIFICATION = "Creating a notification without download button"

        val NCTAG = ".NetworkConnection"
        val CONNECTION = "Connecting to GitHub and getting latest information"
        val JSONERROR = "Error while trying to read JSON from URL. Full trace: "
    }
}
