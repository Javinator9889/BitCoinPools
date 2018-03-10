package javinator9889.bitcoinpools;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.metrics.AddTrace;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javinator9889.bitcoinpools.AppUpdaterManager.CheckUpdates;
import javinator9889.bitcoinpools.FragmentViews.DonationsActivity;
import javinator9889.bitcoinpools.FragmentViews.Tab1PoolsChart;
import javinator9889.bitcoinpools.FragmentViews.Tab2BTCChart;
import javinator9889.bitcoinpools.JSONTools.JSONTools;


@SuppressLint("StaticFieldLeak")
public class MainActivity extends AppCompatActivity {
    public static Toolbar MAINACTIVITY_TOOLBAR;
    public static AppCompatActivity mainActivity;

    private float mpu;
    private HashMap<String, Float> retrievedData;
    private HashMap<String, Float> cardsData;
    private HashMap<Date, Float> btcPrice;
    private EasterEgg easterEgg;

    @Override
    @SuppressWarnings("unchecked")
    @AddTrace(name = "onCreateMainActivity")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_MAINVIEW);
        setContentView(R.layout.activity_main);
        Intent dataFromDataLoaderClass = getIntent();
        this.mpu = dataFromDataLoaderClass.getFloatExtra("MPU", 0);
        this.retrievedData = JSONTools.sortByValue
                ((HashMap<String, Float>) dataFromDataLoaderClass
                        .getSerializableExtra("RD"));
        this.cardsData = JSONTools.sortByValue
                ((HashMap<String, Float>) dataFromDataLoaderClass
                        .getSerializableExtra("CARDS"));
        this.btcPrice = JSONTools.sortDateByValue
                ((HashMap<Date, Float>) dataFromDataLoaderClass
                        .getSerializableExtra("BTCPRICE"));
        if (BitCoinApp.getSharedPreferences().contains(Constants.SHARED_PREFERENCES.APP_VERSION)) {
            if (!BitCoinApp.getSharedPreferences()
                    .getString(Constants.SHARED_PREFERENCES.APP_VERSION, "1.0")
                    .equals(BitCoinApp.appVersion()))
            {
                new MaterialDialog.Builder(this)
                        .title("Changelog")
                        .content(R.string.changelog,
                                true)
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
                    .content(R.string.changelog,
                            true)
                    .cancelable(true)
                    .positiveText(R.string.accept)
                    .build().show();
            SharedPreferences.Editor editor = BitCoinApp.getSharedPreferences().edit();
            editor.putString(Constants.SHARED_PREFERENCES.APP_VERSION, BitCoinApp.appVersion());
            editor.apply();
        }

        Log.d(Constants.LOG.MATAG, Constants.LOG.INIT_VALUES);

        CheckUpdates ck = new CheckUpdates(Constants.GITHUB_USER, Constants.GITHUB_REPO);

        SectionsPagerAdapter mSectionsPagerAdapter =
                new SectionsPagerAdapter(getSupportFragmentManager());
        MAINACTIVITY_TOOLBAR = findViewById(R.id.toolbar);
        setSupportActionBar(MAINACTIVITY_TOOLBAR);

        ViewPager viewPager = findViewById(R.id.viewContainer);
        viewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        setupTabs(tabLayout);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));

        this.easterEgg = EasterEgg.newInstance(getResources());
        MAINACTIVITY_TOOLBAR.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (easterEgg.addStep(MainActivity.this)) {
                    Intent easterEggIntent = new Intent(MainActivity.this, EasterEgg.class);
                    startActivity(easterEggIntent);
                    overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                    easterEgg.resetSteps();
                }
                return false;
            }
        });

        Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_CHART);
        Log.d(Constants.LOG.MATAG, Constants.LOG.LISTENING);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, "MainActivity created");
        BitCoinApp.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        try {
            ck.checkForUpdates(this,
                    getString(R.string.updateAvailable),
                    getString(R.string.updateDescrip),
                    getString(R.string.updateNow),
                    getString(R.string.updateLater),
                    getString(R.string.updatePage));
        } catch (NullPointerException e) {
            Log.e(Constants.LOG.MATAG, "Unable to get updates");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataLoaderScreen.dataLoaderScreenActivity.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void refresh() {
        Tab2BTCChart.setLineChartCreated();
        Intent intentMain = new Intent(MainActivity.this, DataLoaderScreen.class);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        /*intentMain.putExtra("MPU", mpu);
        intentMain.putExtra("RD", retrievedData);
        intentMain.putExtra("CARDS", cardsData);
        intentMain.putExtra("BTCPRICE", btcPrice);*/
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
                        Intent intentSettings = new Intent(MainActivity.this,
                                SpinnerActivity.class);
                        startActivity(intentSettings);
                        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                    }
                };
                settingsThread.setName("settings_thread");
                settingsThread.start();
                break;
            case R.id.license:
                Thread licenseThread = new Thread() {
                    public void run() {
                        Intent intentLicense = new Intent(MainActivity.this,
                                License.class);
                        startActivity(intentLicense);
                        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
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
                Intent shareAppIntent = new Intent(Intent.ACTION_SEND);
                shareAppIntent.setType("text/plain");
                shareAppIntent.putExtra(Intent.EXTRA_SUBJECT, "BitCoin Pools");
                final Uri googlePlayLink = Uri.parse(Constants.GOOGLE_PLAY_URL);
                shareAppIntent.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.inv_message) +
                                " - Google Play Store: " +
                                googlePlayLink.toString());
                startActivity(Intent.createChooser(shareAppIntent,
                        getString(R.string.invitation_title)));
                break;
            case R.id.donate:
                Intent donateIntent = new Intent(MainActivity.this,
                        DonationsActivity.class);
                startActivity(donateIntent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                break;
        }
        return true;
    }

    private void setupTabs(TabLayout destinationTab) {
        destinationTab.addTab(destinationTab.newTab().
                setText(R.string.poolsChart).setIcon(R.drawable.ic_poll_white_24dp));
        destinationTab.addTab(destinationTab.newTab().
                setText(R.string.bitcoinChart).setIcon(R.drawable.ic_attach_money_white_24dp));
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
            switch (position) {
                case 0:
                    return Tab1PoolsChart.newInstance(mpu, retrievedData);
                case 1:
                    return Tab2BTCChart.newInstance(cardsData, btcPrice);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

    }
}

