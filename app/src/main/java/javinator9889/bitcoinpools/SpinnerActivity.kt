package javinator9889.bitcoinpools

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import javinator9889.bitcoinpools.Constants.LOG.CHANGE_PREFERENCES
import javinator9889.bitcoinpools.Constants.LOG.STAG
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.CACHE_JOB_PERIOD
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.CUSTOM_POOLS
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.CUSTOM_PRICE
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.DAYS_TO_CHECK
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.HAS_USER_DEFINED_CUSTOM_PRICE
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.NOTIFICATIONS_ENABLED
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.USER_LANGUAGE
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.VALUE_TO_CHECK
import javinator9889.bitcoinpools.Constants.TRANSLATION_URL
import java.util.*

/**
 * Created by Javinator9889 on 22/12/2017. Settings class
 */

class SpinnerActivity : BaseActivity(), AdapterView.OnItemSelectedListener {
    //    private static final SparseIntArray PRICES_VALUES = new SparseIntArray();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(STAG, Constants.LOG.INIT_SETTINGS_VIEW)
        setContentView(R.layout.settings)
        setTitle(R.string.settingsTitle)
        HAS_CHANGED_LANGUAGE = false
        //        initArrayOfValues();

        val preferences = BitCoinApp.sharedPreferences

        ACTUAL_DAYS = preferences!!.getInt(DAYS_TO_CHECK, 1)
        val hasUserDefinedCustomPrice = preferences.getBoolean(HAS_USER_DEFINED_CUSTOM_PRICE, false)
        ACTUAL_PRICE = if (!hasUserDefinedCustomPrice)
            preferences.getInt(VALUE_TO_CHECK, 1000)
        else
            preferences.getFloat(CUSTOM_PRICE, 1000f)
        ACTUAL_ENABLED = preferences.getBoolean(NOTIFICATIONS_ENABLED, false)
        ACTUAL_PERIOD = preferences.getInt(CACHE_JOB_PERIOD, 1)
        POOLS_TO_SHOW = preferences.getInt(CUSTOM_POOLS, 10)
        LANGUAGE = preferences.getString(USER_LANGUAGE, "system")
        NEW_VALUE_ENABLED = ACTUAL_ENABLED

        //        TextView tv = findViewById(R.id.daysTitle);
        val spinner = findViewById<Spinner>(R.id.spinner2)

        Log.d(STAG, Constants.LOG.INIT_SPINNER)
        spinner.onItemSelectedListener = this
        val adapter = ArrayAdapter
                .createFromResource(this, R.array.days,
                        android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(ACTUAL_DAYS - 1)

        val editText = findViewById<PrefixEditText>(R.id.customPrice)
        editText.setText(String.format(Locale.US, "%.2f", ACTUAL_PRICE))
        editText.isEnabled = ACTUAL_ENABLED

        val cacheDaysSpinner = findViewById<Spinner>(R.id.cacheSpinner2)
        cacheDaysSpinner.onItemSelectedListener = this
        val cacheDays = ArrayAdapter
                .createFromResource(this, R.array.cache_periods,
                        android.R.layout.simple_spinner_item)
        cacheDays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        cacheDaysSpinner.adapter = cacheDays
        cacheDaysSpinner.setSelection(getCacheSpinnerPositionByDay(ACTUAL_PERIOD))

        Log.d(STAG, Constants.LOG.INIT_SWITCH + ACTUAL_ENABLED)
        val settingsSwitch = findViewById<Switch>(R.id.switch1)
        settingsSwitch.isChecked = ACTUAL_ENABLED
        settingsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            Log.d(STAG, CHANGE_PREFERENCES + "switch (enables/disables options)")
            if (isChecked) {
                val sharedPreferencesEditor = preferences.edit()
                sharedPreferencesEditor.putBoolean(NOTIFICATIONS_ENABLED, true)
                sharedPreferencesEditor.apply()
                editText.isEnabled = true
                NEW_VALUE_ENABLED = true
            } else {
                val sharedPreferencesEditor = preferences.edit()
                sharedPreferencesEditor.putBoolean(NOTIFICATIONS_ENABLED, false)
                sharedPreferencesEditor.apply()
                editText.isEnabled = false
                NEW_VALUE_ENABLED = false
            }
        }

        val poolsToShow = findViewById<Spinner>(R.id.poolsNumberText)
        poolsToShow.onItemSelectedListener = this
        val poolsNumber = ArrayAdapter
                .createFromResource(this, R.array.pools_numbers,
                        android.R.layout.simple_spinner_item)
        poolsNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        poolsToShow.adapter = poolsNumber
        poolsToShow.setSelection(POOLS_TO_SHOW - 3)

        val languageText = findViewById<TextView>(R.id.languageText)
        languageText.text = switchLocale(LANGUAGE!!)
        languageText.setOnClickListener {
            val languageSelectionDialog = MaterialDialog.Builder(this@SpinnerActivity)
                    .title(R.string.language)
                    .content(R.string.language_dialog_desc)
                    .icon(getDrawable(R.drawable.ic_baseline_language_24px)!!)
                    .items(this@SpinnerActivity.getString(R.string.system),
                            "English",
                            "Castellano")
                    .itemsCallbackSingleChoice(switchLocalePosition(LANGUAGE!!)
                    ) { dialog, itemView, which, text ->
                        when (which) {
                            0 -> LANGUAGE = "system"
                            1 -> LANGUAGE = "en"
                            2 -> LANGUAGE = "es"
                            else -> LANGUAGE = "system"
                        }
                        this@SpinnerActivity.updateLanguage()
                        false
                    }
                    .positiveText(R.string.accept)
                    .negativeText(R.string.cancel)
                    .neutralText(R.string.contribute_translation)
                    .onNeutral { dialog, which ->
                        val showBrowserIntent = Intent(Intent.ACTION_VIEW)
                        showBrowserIntent.data = Uri.parse(TRANSLATION_URL)
                        startActivity(showBrowserIntent)
                    }
                    .build()
            languageSelectionDialog.show()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (parent.id) {
            R.id.spinner2 -> {
                Log.d(STAG, CHANGE_PREFERENCES + "spinner2 (days to check)")
                NEW_VALUE_DAYS = Integer.parseInt(parent.getItemAtPosition(position).toString())
                val sharedPreferencesEditor = BitCoinApp
                        .sharedPreferences!!.edit()
                sharedPreferencesEditor.putInt(DAYS_TO_CHECK,
                        NEW_VALUE_DAYS)
                sharedPreferencesEditor.apply()
            }
            R.id.poolsNumberText -> {
                Log.d(STAG, CHANGE_PREFERENCES + "changing pools to show values")
                NEW_POOLS_TO_SHOW = Integer.parseInt(parent.getItemAtPosition(position).toString())
                BitCoinApp.sharedPreferences!!.edit()
                        .putInt(CUSTOM_POOLS, NEW_POOLS_TO_SHOW)
                        .apply()
            }
            R.id.cacheSpinner2 -> {
                Log.d(STAG, CHANGE_PREFERENCES + "spinner4 (cache period)")
                NEW_PERIOD = getDaysByItemPosition(position)
                val editor = BitCoinApp.sharedPreferences!!.edit()
                editor.putInt(CACHE_JOB_PERIOD, NEW_PERIOD)
                editor.apply()
            }
            else -> Log.e(STAG, Constants.LOG.UNCAUGHT_ERROR + "SpinnerActivity.onItemSelected(AdapterView<?> parent, View view, int position, long id)",
                    UnknownError())
        }
    }

    private fun updateLanguage() {
        BitCoinApp.localeManager.setNewLocale(this, LANGUAGE)
        HAS_CHANGED_LANGUAGE = true
        val languageText = findViewById<TextView>(R.id.languageText)
        languageText.text = switchLocale(LANGUAGE!!)
        Toast.makeText(this, R.string.language_changed, Toast.LENGTH_LONG).show()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {}

    @SuppressLint("ApplySharedPref")
    override fun onBackPressed() {
        Log.d(STAG, Constants.LOG.BACK_TO_MC)
        val editText = findViewById<PrefixEditText>(R.id.customPrice)
        NEW_VALUE_PRICE = java.lang.Float.valueOf(editText.text!!.toString())
        if (ACTUAL_PRICE != NEW_VALUE_PRICE) {
            val editor = BitCoinApp.sharedPreferences!!.edit()
            editor.putFloat(CUSTOM_PRICE, NEW_VALUE_PRICE)
            editor.putBoolean(HAS_USER_DEFINED_CUSTOM_PRICE, true)
            editor.commit()
        }

        if (hasAnyValueChanged()) {
            if (mustRestartBackgroundJobs()) {
                Thread(Runnable { BitCoinApp.forceRestartBackgroundJobs() }).start()
            }
            Toast.makeText(this, R.string.prefUpdated, Toast.LENGTH_LONG).show()
            if (mustRestartActivity())
                refresh()
        }
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
    }

    private fun mustRestartBackgroundJobs(): Boolean {
        return HAS_CHANGED_LANGUAGE ||
                NEW_VALUE_ENABLED && !ACTUAL_ENABLED ||
                ACTUAL_PERIOD != NEW_PERIOD
    }

    private fun hasAnyValueChanged(): Boolean {
        return NEW_VALUE_DAYS != ACTUAL_DAYS ||
                NEW_VALUE_PRICE != ACTUAL_PRICE ||
                NEW_VALUE_ENABLED != ACTUAL_ENABLED ||
                ACTUAL_PERIOD != NEW_PERIOD ||
                POOLS_TO_SHOW != NEW_POOLS_TO_SHOW ||
                HAS_CHANGED_LANGUAGE
    }

    private fun mustRestartActivity(): Boolean {
        return HAS_CHANGED_LANGUAGE ||
                NEW_VALUE_DAYS != ACTUAL_DAYS ||
                NEW_POOLS_TO_SHOW != POOLS_TO_SHOW
    }

    private fun refresh() {
        MainActivity.mainActivity.finish()
        val intentLoader = Intent(this@SpinnerActivity, DataLoaderScreen::class.java)
        intentLoader.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intentLoader)
        finish()
    }

    /*private void initArrayOfValues() {
        PRICES_VALUES.append(1000, 0);
        PRICES_VALUES.append(2000, 1);
        PRICES_VALUES.append(5000, 2);
        PRICES_VALUES.append(7000, 3);
        PRICES_VALUES.append(8000, 4);
        PRICES_VALUES.append(9000, 5);
        PRICES_VALUES.append(10000, 6);
        PRICES_VALUES.append(11000, 7);
        PRICES_VALUES.append(12000, 8);
        PRICES_VALUES.append(13000, 9);
        PRICES_VALUES.append(14000, 10);
        PRICES_VALUES.append(15000, 11);
        PRICES_VALUES.append(16000, 12);
        PRICES_VALUES.append(17000, 13);
        PRICES_VALUES.append(18000, 14);
        PRICES_VALUES.append(19000, 15);
        PRICES_VALUES.append(20000, 16);
        PRICES_VALUES.append(25000, 17);
        PRICES_VALUES.append(30000, 18);
    }*/

    private fun getCacheSpinnerPositionByDay(days: Int): Int {
        when (days) {
            1 -> return 0
            3 -> return 1
            7 -> return 2
            14 -> return 3
            30 -> return 4
            90 -> return 5
            365 -> return 6
            else -> return 0
        }
    }

    private fun getDaysByItemPosition(position: Int): Int {
        when (position) {
            0 -> return 1
            1 -> return 3
            2 -> return 7
            3 -> return 14
            4 -> return 30
            5 -> return 90
            6 -> return 365
            else -> return 1
        }
    }

    private fun switchLocale(language: String): String {
        when (language) {
            "en" -> return "English"
            "es" -> return "Castellano"
            else -> return getString(R.string.system)
        }
    }

    private fun switchLocalePosition(language: String): Int {
        when (language) {
            "en" -> return 1
            "es" -> return 2
            else -> return 0
        }
    }

    companion object {
        private var ACTUAL_DAYS = 1
        private var ACTUAL_PRICE = 1000f
        private var NEW_VALUE_DAYS = 0
        private var NEW_VALUE_PRICE = 0f
        private var ACTUAL_ENABLED = false
        private var NEW_VALUE_ENABLED = false
        private var ACTUAL_PERIOD = 1
        private var NEW_PERIOD = 0
        private var POOLS_TO_SHOW = 10
        private var NEW_POOLS_TO_SHOW = 10
        private var LANGUAGE: String? = null
        private var HAS_CHANGED_LANGUAGE = false
    }
}
