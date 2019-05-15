package javinator9889.bitcoinpools.FragmentViews

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_META_DATA
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import javinator9889.bitcoinpools.BitCoinApp
import javinator9889.bitcoinpools.Constants.PAYMENTS.GOOGLE_CATALOG
import javinator9889.bitcoinpools.Constants.PAYMENTS.GOOGLE_PUBKEY
import javinator9889.bitcoinpools.R
import org.sufficientlysecure.donations.BuildConfig
import org.sufficientlysecure.donations.DonationsFragment

/**
 * Created by Javinator9889 on 01/03/2018. Based on lib: https://github.com/PrivacyApps/donations
 */

class DonationsActivity : FragmentActivity() {
    private var donationsFragment: DonationsFragment? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetActivityTitle()
        setContentView(R.layout.donations_activity)

        if (isGooglePlayServicesAvailable(this)) { // && (Build.VERSION.SDK_INT > 22)) {
            val fragmentTransaction = supportFragmentManager
                    .beginTransaction()

            this.donationsFragment = DonationsFragment.newInstance(BuildConfig.DEBUG, true,
                    GOOGLE_PUBKEY, GOOGLE_CATALOG,
                    resources.getStringArray(R.array.donation_google_catalog_values),
                    false, null, null, null, false, null, null, false, null)

            fragmentTransaction.replace(R.id.donations_activity_container, this.donationsFragment!!,
                    "donationsFragment")
            fragmentTransaction.commit()
        } else {
            val noDonationsAvailableText = findViewById<TextView>(R.id.no_donations_available)
            noDonationsAvailableText.setTextColor(Color.RED)
            noDonationsAvailableText.setText(R.string.noDonationsAvailable)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        try {
            val googleDonationsButton = donationsFragment!!.activity!!
                    .findViewById<Button>(R.id.donations__google_android_market_donate_button)
            googleDonationsButton.setOnClickListener { v ->
                try {
                    donationsFragment!!.donateGoogleOnClick(v)
                } catch (e: IllegalStateException) {
                    MaterialDialog.Builder(this@DonationsActivity)
                            .title(R.string.donations__google_android_market_not_supported_title)
                            .content(R.string.donations__google_android_market_not_supported)
                            .cancelable(true)
                            .positiveText(R.string.accept)
                            .build().show()

                }
            }
        } catch (e: NullPointerException) {
            Log.e("DonationsActivity", "Unable to get button-fragment. Full trace: " + e.message)
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentByTag("donationsFragment")
        fragment?.onActivityResult(requestCode, resultCode, data)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.TRANSACTION_ID, resultCode.toString())
        bundle.putBundle(FirebaseAnalytics.Param.VALUE, data!!.extras)
        BitCoinApp.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, bundle)
    }

    fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailabilityForPlayServices = GoogleApiAvailability
                .getInstance()
        val status = googleApiAvailabilityForPlayServices.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailabilityForPlayServices.isUserResolvableError(status)) {
                googleApiAvailabilityForPlayServices.getErrorDialog(activity, status, 2404)
                        .show()
            }
            return false
        }
        return true
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(BitCoinApp.localeManager.setLocale(base))
    }

    private fun resetActivityTitle() {
        try {
            val info = packageManager.getActivityInfo(componentName,
                    GET_META_DATA)
            if (info.labelRes != 0) {
                setTitle(info.labelRes)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("Donations", "Unable to change activity title - maybe it is not set?", e)
        }

    }
}
