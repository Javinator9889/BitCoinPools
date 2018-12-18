package javinator9889.bitcoinpools;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static javinator9889.bitcoinpools.Constants.LOG.CHANGE_PREFERENCES;
import static javinator9889.bitcoinpools.Constants.LOG.STAG;
import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.CACHE_JOB_PERIOD;
import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.CUSTOM_POOLS;
import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.CUSTOM_PRICE;
import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.DAYS_TO_CHECK;
import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.HAS_USER_DEFINED_CUSTOM_PRICE;
import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.NOTIFICATIONS_ENABLED;
import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.USER_LANGUAGE;
import static javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.VALUE_TO_CHECK;
import static javinator9889.bitcoinpools.Constants.TRANSLATION_URL;

/**
 * Created by Javinator9889 on 22/12/2017. Settings class
 */

public class SpinnerActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    private static int ACTUAL_DAYS = 1;
    private static float ACTUAL_PRICE = 1000;
    private static int NEW_VALUE_DAYS = 0;
    private static float NEW_VALUE_PRICE = 0;
    private static boolean ACTUAL_ENABLED = false;
    private static boolean NEW_VALUE_ENABLED = false;
    private static int ACTUAL_PERIOD = 1;
    private static int NEW_PERIOD = 0;
    private static int POOLS_TO_SHOW = 10;
    private static int NEW_POOLS_TO_SHOW = 10;
    private static String LANGUAGE;
    private static boolean HAS_CHANGED_LANGUAGE = false;
//    private static final SparseIntArray PRICES_VALUES = new SparseIntArray();

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(STAG, Constants.LOG.INIT_SETTINGS_VIEW);
        setContentView(R.layout.settings);
        setTitle(R.string.settingsTitle);
        HAS_CHANGED_LANGUAGE = false;
//        initArrayOfValues();

        final SharedPreferences preferences = BitCoinApp.getSharedPreferences();

        ACTUAL_DAYS = preferences.getInt(DAYS_TO_CHECK, 1);
        boolean hasUserDefinedCustomPrice = preferences.getBoolean
                (HAS_USER_DEFINED_CUSTOM_PRICE, false);
        ACTUAL_PRICE = !hasUserDefinedCustomPrice ?
                preferences.getInt(VALUE_TO_CHECK, 1000) :
                preferences.getFloat(CUSTOM_PRICE, 1000f);
        ACTUAL_ENABLED = preferences.getBoolean(NOTIFICATIONS_ENABLED, false);
        ACTUAL_PERIOD = preferences.getInt(CACHE_JOB_PERIOD, 1);
        POOLS_TO_SHOW = preferences.getInt(CUSTOM_POOLS, 10);
        LANGUAGE = preferences.getString(USER_LANGUAGE, "system");
        NEW_VALUE_ENABLED = ACTUAL_ENABLED;

//        TextView tv = findViewById(R.id.daysTitle);
        final Spinner spinner = findViewById(R.id.spinner2);

        Log.d(STAG, Constants.LOG.INIT_SPINNER);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.days,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(ACTUAL_DAYS - 1);

        final PrefixEditText editText = findViewById(R.id.customPrice);
        editText.setText(String.format(Locale.US, "%.2f", ACTUAL_PRICE));
        editText.setEnabled(ACTUAL_ENABLED);

        final Spinner cacheDaysSpinner = findViewById(R.id.cacheSpinner2);
        cacheDaysSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> cacheDays = ArrayAdapter
                .createFromResource(this, R.array.cache_periods,
                        android.R.layout.simple_spinner_item);
        cacheDays.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cacheDaysSpinner.setAdapter(cacheDays);
        cacheDaysSpinner.setSelection(getCacheSpinnerPositionByDay(ACTUAL_PERIOD));

        Log.d(STAG, Constants.LOG.INIT_SWITCH + ACTUAL_ENABLED);
        final Switch settingsSwitch = findViewById(R.id.switch1);
        settingsSwitch.setChecked(ACTUAL_ENABLED);
        settingsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(STAG, CHANGE_PREFERENCES
                        + "switch (enables/disables options)");
                if (isChecked) {
                    SharedPreferences.Editor sharedPreferencesEditor = preferences.edit();
                    sharedPreferencesEditor.putBoolean(NOTIFICATIONS_ENABLED, true);
                    sharedPreferencesEditor.apply();
                    editText.setEnabled(true);
                    NEW_VALUE_ENABLED = true;
                } else {
                    SharedPreferences.Editor sharedPreferencesEditor = preferences.edit();
                    sharedPreferencesEditor.putBoolean(NOTIFICATIONS_ENABLED, false);
                    sharedPreferencesEditor.apply();
                    editText.setEnabled(false);
                    NEW_VALUE_ENABLED = false;
                }
            }
        });

        final Spinner poolsToShow = findViewById(R.id.poolsNumberText);
        poolsToShow.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> poolsNumber = ArrayAdapter
                .createFromResource(this, R.array.pools_numbers,
                        android.R.layout.simple_spinner_item);
        poolsNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        poolsToShow.setAdapter(poolsNumber);
        poolsToShow.setSelection(POOLS_TO_SHOW - 3);

        final TextView languageText = findViewById(R.id.languageText);
        languageText.setText(switchLocale(LANGUAGE));
        languageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog languageSelectionDialog = new MaterialDialog
                        .Builder(SpinnerActivity.this)
                        .title(R.string.language)
                        .content(R.string.language_dialog_desc)
                        .icon(getDrawable(R.drawable.ic_baseline_language_24px))
                        .items(SpinnerActivity.this.getString(R.string.system),
                                "English",
                                "Castellano")
                        .itemsCallbackSingleChoice(switchLocalePosition(LANGUAGE),
                                new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog,
                                                               View itemView,
                                                               int which,
                                                               CharSequence text) {
                                        switch (which) {
                                            case 0:
                                                LANGUAGE = "system";
                                                break;
                                            case 1:
                                                LANGUAGE = "en";
                                                break;
                                            case 2:
                                                LANGUAGE = "es";
                                                break;
                                            default:
                                                LANGUAGE = "system";
                                                break;
                                        }
                                        SpinnerActivity.this.updateLanguage();
                                        return false;
                                    }
                                })
                        .positiveText(R.string.accept)
                        .negativeText(R.string.cancel)
                        .neutralText(R.string.contribute_translation)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
                                Intent showBrowserIntent = new Intent(Intent.ACTION_VIEW);
                                showBrowserIntent.setData(Uri.parse(TRANSLATION_URL));
                                startActivity(showBrowserIntent);
                            }
                        })
                        .build();
                languageSelectionDialog.show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner2:
                Log.d(STAG, CHANGE_PREFERENCES
                        + "spinner2 (days to check)");
                NEW_VALUE_DAYS = Integer.parseInt(parent.getItemAtPosition(position).toString());
                SharedPreferences.Editor sharedPreferencesEditor = BitCoinApp
                        .getSharedPreferences().edit();
                sharedPreferencesEditor.putInt(DAYS_TO_CHECK,
                        NEW_VALUE_DAYS);
                sharedPreferencesEditor.apply();
                break;
            case R.id.poolsNumberText:
                Log.d(STAG, CHANGE_PREFERENCES + "changing pools to show values");
                NEW_POOLS_TO_SHOW = Integer.parseInt(parent.getItemAtPosition(position).toString
                        ());
                BitCoinApp.getSharedPreferences().edit()
                        .putInt(CUSTOM_POOLS, NEW_POOLS_TO_SHOW)
                        .apply();
                break;
            case R.id.cacheSpinner2:
                Log.d(STAG, CHANGE_PREFERENCES
                        + "spinner4 (cache period)");
                NEW_PERIOD = getDaysByItemPosition(position);
                SharedPreferences.Editor editor = BitCoinApp.getSharedPreferences().edit();
                editor.putInt(CACHE_JOB_PERIOD, NEW_PERIOD);
                editor.apply();
                break;
            default:
                Log.e(STAG, Constants.LOG.UNCAUGHT_ERROR
                                + "SpinnerActivity.onItemSelected(AdapterView<?> parent, View view, int position, long id)",
                        new UnknownError());
                break;
        }
    }

    private void updateLanguage() {
        BitCoinApp.localeManager.setNewLocale(this, LANGUAGE);
        HAS_CHANGED_LANGUAGE = true;
        TextView languageText = findViewById(R.id.languageText);
        languageText.setText(switchLocale(LANGUAGE));
        Toast.makeText(this, R.string.language_changed, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @SuppressLint("ApplySharedPref")
    @Override
    public void onBackPressed() {
        Log.d(STAG, Constants.LOG.BACK_TO_MC);
        PrefixEditText editText = findViewById(R.id.customPrice);
        NEW_VALUE_PRICE = Float.valueOf(editText.getText().toString());
        if (ACTUAL_PRICE != NEW_VALUE_PRICE) {
            SharedPreferences.Editor editor = BitCoinApp.getSharedPreferences().edit();
            editor.putFloat(CUSTOM_PRICE, NEW_VALUE_PRICE);
            editor.putBoolean(HAS_USER_DEFINED_CUSTOM_PRICE, true);
            editor.commit();
        }

        if (hasAnyValueChanged()) {
            if (mustRestartBackgroundJobs()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BitCoinApp.forceRestartBackgroundJobs();
                    }
                }).start();
            }
            Toast.makeText(this, R.string.prefUpdated, Toast.LENGTH_LONG).show();
            if (mustRestartActivity())
                refresh();
        }
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    private boolean mustRestartBackgroundJobs() {
        return (HAS_CHANGED_LANGUAGE ||
                (NEW_VALUE_ENABLED && !ACTUAL_ENABLED) ||
                (ACTUAL_PERIOD != NEW_PERIOD));
    }

    private boolean hasAnyValueChanged() {
        return ((NEW_VALUE_DAYS != ACTUAL_DAYS) ||
                (NEW_VALUE_PRICE != ACTUAL_PRICE) ||
                (NEW_VALUE_ENABLED != ACTUAL_ENABLED) ||
                (ACTUAL_PERIOD != NEW_PERIOD) ||
                (POOLS_TO_SHOW != NEW_POOLS_TO_SHOW) ||
                HAS_CHANGED_LANGUAGE);
    }

    private boolean mustRestartActivity() {
        return HAS_CHANGED_LANGUAGE ||
                (NEW_VALUE_DAYS != ACTUAL_DAYS) ||
                (NEW_POOLS_TO_SHOW != POOLS_TO_SHOW);
    }

    private void refresh() {
        MainActivity.mainActivity.finish();
        Intent intentLoader = new Intent(SpinnerActivity.this, DataLoaderScreen.class);
        intentLoader.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentLoader);
        finish();
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

    private int getCacheSpinnerPositionByDay(int days) {
        switch (days) {
            case 1:
                return 0;
            case 3:
                return 1;
            case 7:
                return 2;
            case 14:
                return 3;
            case 30:
                return 4;
            case 90:
                return 5;
            case 365:
                return 6;
            default:
                return 0;
        }
    }

    private int getDaysByItemPosition(int position) {
        switch (position) {
            case 0:
                return 1;
            case 1:
                return 3;
            case 2:
                return 7;
            case 3:
                return 14;
            case 4:
                return 30;
            case 5:
                return 90;
            case 6:
                return 365;
            default:
                return 1;
        }
    }

    private String switchLocale(@NonNull String language) {
        switch (language) {
            case "en":
                return "English";
            case "es":
                return "Castellano";
            default:
                return getString(R.string.system);
        }
    }

    private int switchLocalePosition(@NonNull String language) {
        switch (language) {
            case "en":
                return 1;
            case "es":
                return 2;
            default:
                return 0;
        }
    }
}
