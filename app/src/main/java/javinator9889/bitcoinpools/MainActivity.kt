package javinator9889.bitcoinpools

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.tabs.TabLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.metrics.AddTrace
import javinator9889.bitcoinpools.AppUpdaterManager.CheckUpdates
import javinator9889.bitcoinpools.FragmentViews.DonationsActivity
import javinator9889.bitcoinpools.FragmentViews.Tab1PoolsChart
import javinator9889.bitcoinpools.FragmentViews.Tab2BTCChart
import javinator9889.bitcoinpools.JSONTools.JSONTools
import java.math.BigDecimal
import java.util.*


@SuppressLint("StaticFieldLeak")
class MainActivity : BaseActivity() {

    private var mpu: Float = 0.toFloat()
    private var retrievedData: HashMap<String, Float>? = null
    private var cardsData: HashMap<String, Float>? = null
    private var btcPrice: HashMap<Date, Float>? = null
    private var easterEgg: EasterEgg? = null

    @AddTrace(name = "onCreateMainActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = this
        Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_MAINVIEW)
        setContentView(R.layout.activity_main)
        val dataFromDataLoaderClass = intent
        this.mpu = dataFromDataLoaderClass.getFloatExtra("MPU", 0f)
        this.retrievedData = JSONTools.sortByValue(dataFromDataLoaderClass
                .getSerializableExtra("RD") as HashMap<String, Float>)
        this.cardsData = JSONTools.sortByValue(dataFromDataLoaderClass
                .getSerializableExtra("CARDS") as HashMap<String, Float>)
        this.btcPrice = JSONTools.sortDateByValue(dataFromDataLoaderClass
                .getSerializableExtra("BTCPRICE") as HashMap<Date, Float>)
        if (BitCoinApp.sharedPreferences!!.contains(Constants.SHARED_PREFERENCES.APP_VERSION)) {
            if (BitCoinApp.sharedPreferences!!
                            .getString(Constants.SHARED_PREFERENCES.APP_VERSION, "1.0") != BitCoinApp.appVersion(this)) {
                MaterialDialog.Builder(this)
                        .title("Changelog")
                        .content(R.string.changelog,
                                true)
                        .cancelable(true)
                        .positiveText(R.string.accept)
                        .build().show()
                val editor = BitCoinApp.sharedPreferences!!.edit()
                editor.putString(Constants.SHARED_PREFERENCES.APP_VERSION, BitCoinApp.appVersion(this))
                editor.apply()
            }
        } else {
            MaterialDialog.Builder(this)
                    .title("Changelog")
                    .content(R.string.changelog,
                            true)
                    .cancelable(true)
                    .positiveText(R.string.accept)
                    .build().show()
            val editor = BitCoinApp.sharedPreferences!!.edit()
            editor.putString(Constants.SHARED_PREFERENCES.APP_VERSION, BitCoinApp.appVersion(this))
            editor.apply()
        }

        Log.d(Constants.LOG.MATAG, Constants.LOG.INIT_VALUES)

        val ck = CheckUpdates(Constants.GITHUB_USER, Constants.GITHUB_REPO)

        val mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        MAINACTIVITY_TOOLBAR = findViewById(R.id.settingsToolbar)
        setSupportActionBar(MAINACTIVITY_TOOLBAR)

        val viewPager = findViewById<ViewPager>(R.id.viewContainer)
        viewPager.adapter = mSectionsPagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        setupTabs(tabLayout)
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))

        this.easterEgg = EasterEgg.newInstance(resources)
        MAINACTIVITY_TOOLBAR.setOnLongClickListener {
            if (easterEgg!!.addStep(this@MainActivity)) {
                val easterEggIntent = Intent(this@MainActivity,
                        EasterEgg::class.java)
                startActivity(easterEggIntent)
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
                easterEgg!!.resetSteps()
            }
            false
        }

        Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_CHART)
        Log.d(Constants.LOG.MATAG, Constants.LOG.LISTENING)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE, "MainActivity created")
        BitCoinApp.firebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle)
        try {
            ck.checkForUpdates(this,
                    getString(R.string.updateAvailable),
                    getString(R.string.updateDescrip),
                    getString(R.string.updateNow),
                    getString(R.string.updateLater),
                    getString(R.string.updatePage))
        } catch (e: NullPointerException) {
            Log.e(Constants.LOG.MATAG, "Unable to get updates")
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            DataLoaderScreen.dataLoaderScreenActivity.finish()
        } catch (e: NullPointerException) {
            Log.i(Constants.LOG.MATAG, "DataLoaderScreen already finished")
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    private fun refresh() {
        Tab2BTCChart.setLineChartCreated()
        val intentMain = Intent(this@MainActivity, DataLoaderScreen::class.java)
        intentMain.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
        startActivity(intentMain)
        this@MainActivity.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                val settingsThread = object : Thread() {
                    override fun run() {
                        val intentSettings = Intent(this@MainActivity,
                                SpinnerActivity::class.java)
                        startActivity(intentSettings)
                        overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
                    }
                }
                settingsThread.name = "settings_thread"
                settingsThread.start()
            }
            R.id.license -> {
                val licenseThread = object : Thread() {
                    override fun run() {
                        val intentLicense = Intent(this@MainActivity,
                                License::class.java)
                        startActivity(intentLicense)
                        overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
                    }
                }
                licenseThread.name = "license_thread"
                licenseThread.start()
            }
            R.id.update -> {
                refresh()
                Toast.makeText(this, R.string.updated, Toast.LENGTH_LONG).show()
            }
            R.id.share -> {
                val shareAppIntent = Intent(Intent.ACTION_SEND)
                shareAppIntent.type = "text/plain"
                shareAppIntent.putExtra(Intent.EXTRA_SUBJECT, "BitCoin Pools")
                val googlePlayLink = Uri.parse(Constants.GOOGLE_PLAY_URL)
                shareAppIntent.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.inv_message) +
                                " - Google Play Store: " +
                                googlePlayLink.toString())
                startActivity(Intent.createChooser(shareAppIntent,
                        getString(R.string.invitation_title)))
            }
            R.id.donate -> {
                val donateIntent = Intent(this@MainActivity,
                        DonationsActivity::class.java)
                startActivity(donateIntent)
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out)
            }
        }
        return true
    }

    private fun setupTabs(destinationTab: TabLayout) {
        destinationTab.addTab(destinationTab.newTab().setText(R.string.poolsChart).setIcon(R.drawable.ic_poll_white_24dp))
        destinationTab.addTab(destinationTab.newTab().setText(R.string.bitcoinChart).setIcon(R.drawable.ic_attach_money_white_24dp))
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to one of the
     * sections/tabs/pages.
     */
    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return Tab1PoolsChart.newInstance(mpu, retrievedData)
                1 -> return Tab2BTCChart.newInstance(cardsData, btcPrice)
                else -> return null
            }
        }

        override fun getCount(): Int {
            return 2
        }

    }

    companion object {
        var MAINACTIVITY_TOOLBAR: Toolbar
        var mainActivity: AppCompatActivity

        /**
         * Based on: https://stackoverflow.com/questions/8911356/whats-the-best-practice-to-round-a-float-to-2-decimals
         */
        fun round(d: Float, decimalPlace: Int): Float {
            return BigDecimal.valueOf(d.toDouble()).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).toFloat()
        }
    }
}

