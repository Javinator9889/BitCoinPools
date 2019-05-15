package javinator9889.bitcoinpools

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.USER_LANGUAGE
import java.util.*

class LocaleManager {

    private val prefs: SharedPreferences?

    val language: String?
        get() = prefs!!.getString(LANGUAGE_KEY, LANGUAGE_DEFAULT)

    init {
        prefs = BitCoinApp.sharedPreferences
    }

    fun setLocale(c: Context): Context {
        return updateResources(c, language!!)
    }

    fun setNewLocale(c: Context, language: String): Context {
        persistLanguage(language)
        return updateResources(c, language)
    }

    @SuppressLint("ApplySharedPref")
    private fun persistLanguage(language: String) {
        // use commit() instead of apply(), because sometimes we kill the application process immediately
        // which will prevent apply() to finish
        prefs!!.edit().putString(LANGUAGE_KEY, language).commit()
    }

    private fun updateResources(context: Context, language: String): Context {
        var context = context
        val locale: Locale
        if (language == "system") {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                locale = Resources.getSystem().configuration.locale
            else
                locale = Resources.getSystem().configuration.locales.get(0)
        } else
            locale = Locale(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            context = context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
        }
        return context
    }

    companion object {
        val LANGUAGE_ENGLISH = "en"
        val LANGUAGE_SPANISH = "es"
        val LANGUAGE_DEFAULT = "system"
        private val LANGUAGE_KEY = USER_LANGUAGE

        fun getLocale(res: Resources): Locale {
            val config = res.configuration
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                config.locales.get(0)
            else
                config.locale
        }
    }
}