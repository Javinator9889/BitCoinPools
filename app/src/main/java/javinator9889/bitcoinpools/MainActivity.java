package javinator9889.bitcoinpools;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.math.BigDecimal;
import java.util.Calendar;

import javinator9889.bitcoinpools.AppUpdaterManager.CheckUpdates;
import javinator9889.bitcoinpools.FragmentViews.DonationsActivity;
import javinator9889.bitcoinpools.FragmentViews.Tab1PoolsChart;
import javinator9889.bitcoinpools.FragmentViews.Tab2BTCChart;


public class MainActivity extends AppCompatActivity {
    @SuppressLint("StaticFieldLeak")
    public static Toolbar MAINACTIVITY_TOOLBAR;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BitCoinApp.isOnline()) {
            Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_MAINVIEW);
            setContentView(R.layout.activity_main);
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            Bundle appOpen = new Bundle();
            appOpen.putString("Date", Calendar.getInstance().getTime().toString());
            appOpen.putString("DeviceBrand", Build.BRAND);
            appOpen.putString("DeviceID", Build.ID);
            appOpen.putString("DeviceName", Build.PRODUCT);
            appOpen.putString("AndroidVersion", Build.VERSION.RELEASE);
            mFirebaseAnalytics.logEvent("main_activity", appOpen);

            if (BitCoinApp.getSharedPreferences().contains(Constants.SHARED_PREFERENCES.APP_VERSION)) {
                if (!BitCoinApp.getSharedPreferences().getString(Constants.SHARED_PREFERENCES.APP_VERSION, "1.0").equals(BitCoinApp.appVersion())) {
                    new MaterialDialog.Builder(this)
                            .title("Changelog")
                            .content(R.string.changelog, true)
                            .cancelable(true)
                            .positiveText(R.string.accept)
                            .build().show();
                    SharedPreferences.Editor editor = BitCoinApp.getSharedPreferences().edit();
                    editor.putString(Constants.SHARED_PREFERENCES.APP_VERSION, BitCoinApp.appVersion());
                    editor.apply();
                }
            } else {
                new MaterialDialog.Builder(this)
                        .title("Changelog")
                        .content(R.string.changelog, true)
                        .cancelable(true)
                        .positiveText(R.string.accept)
                        .build().show();
                SharedPreferences.Editor editor = BitCoinApp.getSharedPreferences().edit();
                editor.putString(Constants.SHARED_PREFERENCES.APP_VERSION, BitCoinApp.appVersion());
                editor.apply();
            }

            Log.d(Constants.LOG.MATAG, Constants.LOG.INIT_VALUES);
            checkPermissions();
            CheckUpdates ck = new CheckUpdates(Constants.GITHUB_USER, Constants.GITHUB_REPO);

            SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            MAINACTIVITY_TOOLBAR = findViewById(R.id.toolbar);
            setSupportActionBar(MAINACTIVITY_TOOLBAR);

            ViewPager viewPager = findViewById(R.id.viewContainer);
            viewPager.setAdapter(mSectionsPagerAdapter);

            TabLayout tabLayout = findViewById(R.id.tabs);
            setupTabs(tabLayout);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

            Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_CHART);

            Log.d(Constants.LOG.MATAG, Constants.LOG.LISTENING);
            ck.checkForUpdates(this, getString(R.string.updateAvailable), getString(R.string.updateDescrip), getString(R.string.updateNow), getString(R.string.updateLater), getString(R.string.updatePage));
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.noConnectionTitle)
                    .content(R.string.noConnectionDesc)
                    .cancelable(false)
                    .positiveText(R.string.accept)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            closeApp();
                        }
                    })
                    .build()
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void refresh() {
        Tab2BTCChart.setLineChartCreated();
        Intent intentMain = new Intent(MainActivity.this, MainActivity.class);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intentMain);
        MainActivity.this.finish();
    }

    /**
     * Based on: https://stackoverflow.com/questions/8911356/whats-the-best-practice-to-round-a-float-to-2-decimals
     */
    public static float round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Thread settingsThread = new Thread() {
                    public void run() {
                        Intent intentSettings = new Intent(MainActivity.this, SpinnerActivity.class);
                        startActivity(intentSettings);
                        MainActivity.this.finish();
                    }
                };
                settingsThread.setName("settings_thread");
                settingsThread.start();
                break;
            case R.id.license:
                Thread licenseThread = new Thread() {
                    public void run() {
                        Intent intentLicense = new Intent(MainActivity.this, License.class);
                        startActivity(intentLicense);
                    }
                };
                licenseThread.setName("license_thread");
                licenseThread.start();
                break;
            case R.id.update:
                refresh();
                Toast.makeText(this, R.string.updated, Toast.LENGTH_LONG).show();
                break;
            case R.id.share:
                Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                        .setMessage(getString(R.string.inv_message))
                        .setDeepLink(Uri.parse(Constants.GITHUB_URL))
                        .build();
                startActivityForResult(intent, Constants.REQUEST_CODE);
                break;
            case R.id.donate:
                Intent donateIntent = new Intent(MainActivity.this, DonationsActivity.class);
                startActivity(donateIntent);
                break;
        }
        return true;
    }

    private void checkPermissions() {
        Log.d(Constants.LOG.MATAG, Constants.LOG.CHECKING_PERMISSIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    private void closeApp() {
        this.onBackPressed();
    }

    private void setupTabs(TabLayout destinationTab) {
        destinationTab.addTab(destinationTab.newTab().setText(R.string.poolsChart).setIcon(R.drawable.ic_poll_white_24dp));
        destinationTab.addTab(destinationTab.newTab().setText(R.string.bitcoinChart).setIcon(R.drawable.ic_attach_money_white_24dp));
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 0:
                    return new Tab1PoolsChart();
                case 1:
                    return new Tab2BTCChart();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

    }
}

