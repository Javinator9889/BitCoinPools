package javinator9889.bitcoinpools;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Javinator9889 on 22/12/2017.
 * Settings class
 */

public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
    private static int ACTUAL_DAYS = 1;
    private static int ACTUAL_PRICE = 1000;
    private static int NEW_VALUE_DAYS = 0;
    private static int NEW_VALUE_PRICE = 0;
    private static boolean ACTUAL_ENABLED = false;
    private static boolean NEW_VALUE_ENABLED = false;
    private static final SparseIntArray PRICES_VALUES = new SparseIntArray();

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.LOG.STAG, Constants.LOG.INIT_SETTINGS_VIEW);
        setContentView(R.layout.settings);
        setTitle(R.string.settingsTitle);
        initArrayOfValues();

        ACTUAL_DAYS = BitCoinApp.getSharedPreferences()
                .getInt(Constants.SHARED_PREFERENCES.DAYS_TO_CHECK, 1);
        ACTUAL_PRICE = BitCoinApp.getSharedPreferences()
                .getInt(Constants.SHARED_PREFERENCES.VALUE_TO_CHECK, 1000);
        ACTUAL_ENABLED = BitCoinApp.getSharedPreferences()
                .getBoolean(Constants.SHARED_PREFERENCES.NOTIFICATIONS_ENABLED, false);
        NEW_VALUE_ENABLED = ACTUAL_ENABLED;

        TextView tv = findViewById(R.id.daysTitle);
        final Spinner spinner = findViewById(R.id.spinner2);

        Log.d(Constants.LOG.STAG, Constants.LOG.INIT_SPINNER);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.days,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(ACTUAL_DAYS - 1);
        String output = getString(R.string.cDays);
        tv.setText(output);

        final Spinner spinner1 = findViewById(R.id.spinner3);
        spinner1.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter
                .createFromResource(this, R.array.prices,
                        android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setSelection(PRICES_VALUES.get(ACTUAL_PRICE));
        spinner1.setEnabled(ACTUAL_ENABLED);

        Log.d(Constants.LOG.STAG, Constants.LOG.INIT_SWITCH + ACTUAL_ENABLED);
        final Switch settingsSwitch = findViewById(R.id.switch1);
        settingsSwitch.setChecked(ACTUAL_ENABLED);
        settingsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(Constants.LOG.STAG, Constants.LOG.CHANGE_PREFERENCES
                        + "switch (enables/disables options)");
                if (isChecked) {
                    SharedPreferences.Editor sharedPreferencesEditor = BitCoinApp
                            .getSharedPreferences().edit();
                    sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES
                            .NOTIFICATIONS_ENABLED, true);
                    sharedPreferencesEditor.apply();
                    spinner1.setEnabled(true);
                    NEW_VALUE_ENABLED = true;
                } else {
                    SharedPreferences.Editor sharedPreferencesEditor = BitCoinApp
                            .getSharedPreferences().edit();
                    sharedPreferencesEditor.putBoolean(Constants.SHARED_PREFERENCES
                            .NOTIFICATIONS_ENABLED, false);
                    sharedPreferencesEditor.apply();
                    spinner1.setEnabled(false);
                    NEW_VALUE_ENABLED = false;
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner2:
                Log.d(Constants.LOG.STAG, Constants.LOG.CHANGE_PREFERENCES
                        + "spinner2 (days to check)");
                NEW_VALUE_DAYS = Integer.parseInt(parent.getItemAtPosition(position).toString());
                SharedPreferences.Editor sharedPreferencesEditor = BitCoinApp
                        .getSharedPreferences().edit();
                sharedPreferencesEditor.putInt(Constants.SHARED_PREFERENCES.DAYS_TO_CHECK,
                        NEW_VALUE_DAYS);
                sharedPreferencesEditor.apply();
                break;
            case R.id.spinner3:
                Log.d(Constants.LOG.STAG, Constants.LOG.CHANGE_PREFERENCES
                        + "spinner3 (price to check)");
                NEW_VALUE_PRICE = Integer.parseInt(parent.getItemAtPosition(position).toString()
                        .replace("$", ""));
                SharedPreferences.Editor sharedPreferencesEditor2 = BitCoinApp
                        .getSharedPreferences().edit();
                sharedPreferencesEditor2.putInt(Constants.SHARED_PREFERENCES.VALUE_TO_CHECK,
                        NEW_VALUE_PRICE);
                sharedPreferencesEditor2.apply();
                break;
            default:
                Log.e(Constants.LOG.STAG, Constants.LOG.UNCAUGHT_ERROR
                        + "SpinnerActivity.onItemSelected(AdapterView<?> parent, View view, int position, long id)",
                        new UnknownError());
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    @Override
    public void onBackPressed() {
        Log.d(Constants.LOG.STAG, Constants.LOG.BACK_TO_MC);
        if ((NEW_VALUE_DAYS != ACTUAL_DAYS) || (NEW_VALUE_PRICE != ACTUAL_PRICE)
                || (NEW_VALUE_ENABLED != ACTUAL_ENABLED))
        {
            Toast.makeText(this, R.string.prefUpdated, Toast.LENGTH_LONG).show();
            if (NEW_VALUE_ENABLED && !ACTUAL_ENABLED)
                BitCoinApp.forceRestartBackgroundJobs();
            refresh();
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    private void refresh() {
        MainActivity.mainActivity.finish();
        Intent intentMain = new Intent(SpinnerActivity.this, DataLoaderScreen.class);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intentMain);
        SpinnerActivity.this.finish();
    }

    private void initArrayOfValues() {
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
    }
}
