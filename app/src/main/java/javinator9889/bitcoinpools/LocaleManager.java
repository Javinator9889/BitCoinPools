package javinator9889.bitcoinpools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.USER_LANGUAGE;

public class LocaleManager {
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_SPANISH = "es";
    public static final String LANGUAGE_DEFAULT = "system";
    private static final String LANGUAGE_KEY = USER_LANGUAGE;

    private final SharedPreferences prefs;

    public LocaleManager() {
        prefs = BitCoinApp.getSharedPreferences();
    }

    public static Locale getLocale(Resources res) {
        Configuration config = res.getConfiguration();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ?
                config.getLocales().get(0) :
                config.locale;
    }

    public Context setLocale(Context c) {
        return updateResources(c, getLanguage());
    }

    public Context setNewLocale(Context c, String language) {
        persistLanguage(language);
        return updateResources(c, language);
    }

    public String getLanguage() {
        return prefs.getString(LANGUAGE_KEY, LANGUAGE_DEFAULT);
    }

    @SuppressLint("ApplySharedPref")
    private void persistLanguage(String language) {
        // use commit() instead of apply(), because sometimes we kill the application process immediately
        // which will prevent apply() to finish
        prefs.edit().putString(LANGUAGE_KEY, language).commit();
    }

    private Context updateResources(Context context, String language) {
        Locale locale;
        if (language.equals("system")) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                locale = Resources.getSystem().getConfiguration().locale;
            else
                locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else
            locale = new Locale(language);
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }
}