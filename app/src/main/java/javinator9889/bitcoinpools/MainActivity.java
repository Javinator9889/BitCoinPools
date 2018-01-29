package javinator9889.bitcoinpools;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javinator9889.bitcoinpools.AppUpdaterManager.CheckUpdates;
import javinator9889.bitcoinpools.FragmentViews.Tab1PoolsChart;
import javinator9889.bitcoinpools.FragmentViews.Tab2BTCChart;
import javinator9889.bitcoinpools.JSONTools.JSONTools;
import javinator9889.bitcoinpools.NetTools.net;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static Map<String, Float> RETRIEVED_DATA = new LinkedHashMap<>();
    private static float MARKET_PRICE_USD;
    private static ViewGroup.LayoutParams TABLE_PARAMS;
    private Thread rdThread;
    private Thread mpuThread;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BitCoinApp.isOnline()) {
            Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_MAINVIEW);
            setContentView(R.layout.activity_main);

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
            initMPU();
            /*initRD();
            initT();*/
            CheckUpdates ck = new CheckUpdates(Constants.GITHUB_USER, Constants.GITHUB_REPO);
            try {
                mpuThread.join();
            } catch (InterruptedException e) {
                Log.e(Constants.LOG.MATAG, Constants.LOG.JOIN_ERROR);
            } finally {
                printValues();
                //setTitle(getString(R.string.BTCP) + MARKET_PRICE_USD);

                final FloatingActionsMenu mainButton = (FloatingActionsMenu) findViewById(R.id.menu_fab);
                final FloatingActionButton licenseButton = (FloatingActionButton) findViewById(R.id.license);
                final FloatingActionButton closeButton = (FloatingActionButton) findViewById(R.id.close);
                final FloatingActionButton settingsButton = (FloatingActionButton) findViewById(R.id.settings);
                final FloatingActionButton refreshButton = (FloatingActionButton) findViewById(R.id.update);
                //final PieChart chart = (PieChart) findViewById(R.id.chart);
                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

                final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                toolbar.setTitle(getString(R.string.BTCP) + MARKET_PRICE_USD);
                setSupportActionBar(toolbar);
                //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                viewPager = (ViewPager) findViewById(R.id.viewContainer);
                viewPager.setAdapter(mSectionsPagerAdapter);
                //setupViewPager(viewPager);

                tabLayout = (TabLayout) findViewById(R.id.tabs);
                setupTabs(tabLayout);
                viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
                //tabLayout.setupWithViewPager(viewPager);
                //setupTabIcons();

                //viewPager.invalidate();
                /*final ViewPager pager = (ViewPager) findViewById(R.id.tabs);*/

                //pager.setAdapter(getSupportFragmentManager());

                Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_CHART);
                /*createPieChart(chart);
                createTable((TableLayout) findViewById(R.id.poolstable));*/
                mainButton.bringToFront();
                mainButton.invalidate();

                Log.d(Constants.LOG.MATAG, Constants.LOG.LISTENING);
                licenseButton.setOnClickListener(this);
                closeButton.setOnClickListener(this);
                settingsButton.setOnClickListener(this);
                refreshButton.setOnClickListener(this);

                ck.checkForUpdates(this, getString(R.string.updateAvailable), getString(R.string.updateDescrip), getString(R.string.updateNow), getString(R.string.updateLater), getString(R.string.updatePage));
            }
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

    private void refresh() {
        Tab2BTCChart.setLineChartCreated();
        Intent intentMain = new Intent(MainActivity.this, MainActivity.class);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intentMain);
        MainActivity.this.finish();
    }

    private void initMPU() {
        mpuThread = new Thread() {
            public void run() {
                Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_MPU);
                net market = new net();
                market.execute(Constants.STATS_URL);
                try {
                    MARKET_PRICE_USD = round((float) market.get().getDouble(Constants.MARKET_NAME), 2);
                } catch (InterruptedException | ExecutionException | JSONException e) {
                    Log.e(Constants.LOG.MATAG, Constants.LOG.MARKET_PRICE_ERROR + e.getMessage());
                    MARKET_PRICE_USD = 0;
                }
            }
        };
        mpuThread.setName("mpu_thread");
        mpuThread.start();
    }

    private void initRD() {
        rdThread = new Thread() {
            public void run() {
                int days = BitCoinApp.getSharedPreferences().getInt(Constants.SHARED_PREFERENCES.DAYS_TO_CHECK, 1);
                Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_RD);
                String url = Constants.POOLS_URL + days + "days";
                net httpsResponse = new net();
                httpsResponse.execute(url);
                try {
                    RETRIEVED_DATA = JSONTools.sortByValue(JSONTools.convert2HashMap(httpsResponse.get()));
                } catch (InterruptedException | ExecutionException e) {
                    RETRIEVED_DATA = null;
                    Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
                }
            }
        };
        rdThread.setName("rd_thread");
        rdThread.start();
    }

    /*private void initT() {
        TableRow masterRow = (TableRow) findViewById(R.id.masterRow);
        TABLE_PARAMS = masterRow.getLayoutParams();
    }*/

    private void createPieChart(final PieChart destinationChart) {
        Thread pieChartThread = new Thread() {
            public void run() {
                Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_CHART);
                List<PieEntry> values = new ArrayList<>();
                List<Map.Entry<String, Float>> entryList = new ArrayList<>(RETRIEVED_DATA.entrySet());
                Map.Entry<String, Float> getEntry;
                int count = 0;
                for (int i = entryList.size() - 1; (i >= 0) && (count < 10); --i) {
                    getEntry = entryList.get(i);
                    Log.i(Constants.LOG.MATAG, "Accessing at: " + i + " | Key: " + getEntry.getKey() + " | Value: " + getEntry.getValue());
                    values.add(new PieEntry(getEntry.getValue(), getEntry.getKey()));
                    ++count;
                }
                PieDataSet data = new PieDataSet(values, "Latest 24h BTC pools");
                data.setColors(ColorTemplate.MATERIAL_COLORS);
                data.setValueTextSize(10f);

                destinationChart.setData(new PieData(data));
                destinationChart.setUsePercentValues(true);
                destinationChart.setEntryLabelColor(ColorTemplate.rgb("#000000"));
                Description description = new Description();
                description.setText(getString(R.string.porcent));
                destinationChart.setDescription(description);
                destinationChart.getLegend().setEnabled(false);
                destinationChart.invalidate();
            }
        };
        try {
            rdThread.join();
        } catch (InterruptedException e) {
            Log.e(Constants.LOG.MATAG, Constants.LOG.JOIN_ERROR + rdThread.getName());
        } finally {
            pieChartThread.setName("chart_thread");
            pieChartThread.start();
        }
    }

    /*private void createTable(final TableLayout destinationTable) {
        Thread tableThread = new Thread() {
            public void run() {
                Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_TABLE);
                List<Map.Entry<String, Float>> entryList = new ArrayList<>(RETRIEVED_DATA.entrySet());
                Map.Entry<String, Float> getEntry;
                int count = 1;

                TableRow fetchTableRow = findViewById(R.id.masterRow);
                TextView firstPool = new TextView(MainActivity.this);
                TextView firstBlock = new TextView(MainActivity.this);

                getEntry = entryList.get(entryList.size() - 1);

                firstPool.setText(getEntry.getKey());
                firstPool.setTypeface(Typeface.DEFAULT_BOLD);
                firstPool.setTextSize(16f);

                firstBlock.setText(String.valueOf(getEntry.getValue()));
                firstBlock.setTypeface(Typeface.MONOSPACE);
                firstBlock.setTextSize(16f);

                fetchTableRow.addView(firstPool);
                fetchTableRow.addView(firstBlock);
                for (int i = entryList.size() - 2; (i >= 0) && (count <= 10); --i) {
                    TextView poolName = new TextView(MainActivity.this);
                    TextView poolBlock = new TextView(MainActivity.this);
                    TableRow tableRow = new TableRow(MainActivity.this);

                    getEntry = entryList.get(i);
                    poolName.setText(getEntry.getKey());
                    poolBlock.setText(String.valueOf(getEntry.getValue()));

                    poolName.setTypeface(Typeface.DEFAULT_BOLD);
                    poolName.setTextSize(16f);

                    poolBlock.setTypeface(Typeface.MONOSPACE);
                    poolBlock.setTextSize(16f);

                    tableRow.setLayoutParams(TABLE_PARAMS);
                    tableRow.addView(poolName);
                    tableRow.addView(poolBlock);

                    destinationTable.addView(tableRow);
                    ++count;
                }
                destinationTable.invalidate();
            }
        };
        try {
            rdThread.join();
        } catch (InterruptedException e) {
            Log.e(Constants.LOG.MATAG, Constants.LOG.JOIN_ERROR + rdThread.getName());
        } finally {
            tableThread.setName("table_thread");
            tableThread.start();
        }
    }*/

    /**
     * Based on: https://stackoverflow.com/questions/8911356/whats-the-best-practice-to-round-a-float-to-2-decimals
     */
    public static float round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                this.onBackPressed();
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
            case R.id.update:
                refresh();
                Toast.makeText(this, R.string.updated, Toast.LENGTH_LONG).show();
                break;
            default:
                Log.e(Constants.LOG.MATAG, Constants.LOG.UNCAUGHT_ERROR + "MainActivity.onClick(View v)", new UnknownError());
                System.exit(1);
        }
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

    /*private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PoolsView(), "ONE");
        adapter.addFragment(new TwoFragment(), "TWO");
        //adapter.addFragment(new ThreeFragment(), "THREE");
        viewPager.setAdapter(adapter);
    }*/

    /*class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }*/

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_poll_white_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_attach_money_white_24dp);
    }

    private void setupTabs(TabLayout destinationTab) {
        destinationTab.addTab(destinationTab.newTab().setText("Tab 1 - test").setIcon(R.drawable.ic_poll_white_24dp));
        destinationTab.addTab(destinationTab.newTab().setText("Tab 2 - test").setIcon(R.drawable.ic_attach_money_white_24dp));
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
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

    private void printValues() {
        String url = "https://api.coindesk.com/v1/bpi/historical/close.json?start=2011-09-01&end=2018-01-01";
        net httpsResponse = new net();
        httpsResponse.execute(url);
        Map<Date, Float> newData = new LinkedHashMap<>();
        try {
            newData = JSONTools.sortDateByValue(JSONTools.convert2DateHashMap(httpsResponse.get().getJSONObject("bpi")));
        } catch (InterruptedException | ExecutionException | JSONException e) {
            newData = null;
            Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
        }
        /*assert newData != null;
        System.out.println(newData.toString());
        System.out.println(newData.entrySet().toString());
        Iterator<Date> it = newData.keySet().iterator();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        while (it.hasNext()) {
            System.out.println(df.format(it.next()));
        }*/
    }
}

