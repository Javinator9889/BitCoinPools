package javinator9889.bitcoinpools;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import javinator9889.bitcoinpools.AppUpdaterManager.CheckUpdates;
import javinator9889.bitcoinpools.JSONTools.JSONTools;

import static javinator9889.bitcoinpools.MainActivity.round;

/**
 * Created by Javinator9889 on 04/03/2018.
 */

public class DataLoaderScreen extends AppCompatActivity {
    //public static MaterialDialog progressDialog = null;
    public static AppCompatActivity dataLoaderScreenActivity;

    private float mpu;
    private HashMap<String, Float> retrievedData;
    private HashMap<String, Float> cardsData;
    private HashMap<Date, Float> btcPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataLoaderScreenActivity = this;
        if (BitCoinApp.isOnline()) {
            Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_MAINVIEW);
            setContentView(R.layout.activity_loading);
            //mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            /*progressDialog = new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .title(R.string.loadingData)
                    .content(R.string.please_wait)
                    .progress(false, 100)
                    .build();
            progressDialog.show();*/
            new DataLoader().execute();
            //new MainActivity.DataLoader().execute();
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.noConnectionTitle)
                    .content(R.string.noConnectionDesc)
                    .cancelable(false)
                    .positiveText(R.string.accept)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            onBackPressed();
                        }
                    })
                    .build()
                    .show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class DataLoader extends AsyncTask<Void, Void, Boolean> {
        private Thread marketPriceThread;
        private Thread poolsDataThread;
        private Thread cardsDataThread;
        private Thread btcPriceThread;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                Log.i("DLS", "Data loaded...");
                Log.i("DLS", "Data values:\nMPU - " + mpu + "\nRD - " + retrievedData.toString() + "\nCARDS - " + cardsData.toString() + "\nBTCPRICE - " + btcPrice.toString());
                /*if (DataLoaderScreen.this.progressDialog != null) {
                    DataLoaderScreen.this.progressDialog.dismiss();
                }*/
                //setContentView(R.layout.activity_main);
                //completeActivityCreation();
                Intent activityMainIntent = new Intent(DataLoaderScreen.this, MainActivity.class);
                activityMainIntent.putExtra("MPU", mpu);
                activityMainIntent.putExtra("RD", retrievedData);
                activityMainIntent.putExtra("CARDS", cardsData);
                activityMainIntent.putExtra("BTCPRICE", btcPrice);
                startActivity(activityMainIntent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                //DataLoaderScreen.this.finish();
            } else {
                Log.i("DLS", "Data non-loaded...");
                /*if (progressDialog != null) {
                    progressDialog.dismiss();
                }*/
                new MaterialDialog.Builder(DataLoaderScreen.this)
                        .positiveText(R.string.accept)
                        .cancelable(false)
                        .title(R.string.errorLoading)
                        .content(R.string.errorLoadingDescription, true)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                //closeApp();
                                DataLoaderScreen.this.onBackPressed();
                            }
                        })
                        .build().show();
            }
        }

        @Override
        protected Boolean doInBackground(@Nullable Void... params) {
            try {
                getBitCoinMarketPrice();
                getPoolsData();
                getCardsData();
                getBitCoinPriceHistory();
                marketPriceThread.join();
                poolsDataThread.join();
                cardsDataThread.join();
                btcPriceThread.join();
                return true;
            } catch (InterruptedException | DataLoaderException e) {
                Log.e(Constants.LOG.MATAG, Constants.LOG.JOIN_ERROR + e.getMessage());
                return false;
            }
        }

        private JSONObject getHTTPSRequest(String requestUrl) throws Exception {
            StringBuilder response = new StringBuilder();
            URL urlObject = new URL(requestUrl);
            HttpsURLConnection connection = (HttpsURLConnection) urlObject.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();
            return new JSONObject(response.toString());
        }

        private void getBitCoinMarketPrice() {
            marketPriceThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_MPU);
                    /*net market = new net();
                    market.execute(Constants.STATS_URL);
                    try {
                        mpu = round((float) market.get().getDouble(Constants.MARKET_NAME), 2);
                    } catch (InterruptedException | ExecutionException | JSONException e) {
                        Log.e(Constants.LOG.MATAG, Constants.LOG.MARKET_PRICE_ERROR + e.getMessage());
                        mpu = -1;
                    }*/
                    try {
                        mpu = round((float) getHTTPSRequest(Constants.STATS_URL).getDouble(Constants.MARKET_NAME), 2);
                        //progressDialog.setProgress(progressDialog.getCurrentProgress() + 20);
                    } catch (Exception e) {
                        Log.e(Constants.LOG.MATAG, Constants.LOG.MARKET_PRICE_ERROR + e.getMessage());
                        mpu = -1;
                        throw new DataLoaderException("Failed to get data from: " + Constants.STATS_URL);
                    }
                }
            });
            marketPriceThread.start();
        }

        private void getPoolsData() {
            poolsDataThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int days = BitCoinApp.getSharedPreferences().getInt(Constants.SHARED_PREFERENCES.DAYS_TO_CHECK, 1);
                    Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_RD);
                    String url = Constants.POOLS_URL + days + "days";
                    /*net httpsResponse = new net();
                    httpsResponse.execute(url);
                    try {
                        RETRIEVED_DATA = JSONTools.sortByValue(JSONTools.convert2HashMap(httpsResponse.get()));
                    } catch (InterruptedException | ExecutionException e) {
                        RETRIEVED_DATA = null;
                        Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
                    }*/
                    try {
                        retrievedData = JSONTools.convert2HashMap(getHTTPSRequest(url));
                        //progressDialog.setProgress(progressDialog.getCurrentProgress() + 20);
                    } catch (Exception e) {
                        retrievedData = null;
                        Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
                        throw new DataLoaderException("Unable to get data from: " + url);
                    }
                }
            });
            poolsDataThread.start();
        }

        private void getCardsData() {
            cardsDataThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    /*net httpsResponse = new net();
                    Map<String, Float> cardsData = new LinkedHashMap<>();
                    httpsResponse.execute(STATS_URL);
                    try {
                        cardsData = JSONTools.sortByValue(JSONTools.convert2HashMap(httpsResponse.get()));
                    } catch (InterruptedException | ExecutionException e) {
                        cardsData = null;
                        Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
                        Crashlytics.logException(e);
                        }*/
                    try {
                        cardsData = JSONTools.convert2HashMap(getHTTPSRequest(Constants.STATS_URL));
                        //progressDialog.setProgress(progressDialog.getCurrentProgress() + 20);
                    } catch (Exception e) {
                        cardsData = null;
                        Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
                        throw new DataLoaderException("Unable to get data from: " + Constants.STATS_URL);
                    }
                }
            });
            cardsDataThread.start();
        }

        private void getBitCoinPriceHistory() {
            btcPriceThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    /*net httpsResponse = new net();
                    httpsResponse.execute(REQUEST_URL);
                    try {
                        BTCPRICE = JSONTools.sortDateByValue(JSONTools.convert2DateHashMap(httpsResponse.get().getJSONObject("bpi")));
                    } catch (InterruptedException | ExecutionException | JSONException e) {
                        BTCPRICE = null;
                        Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
                    }*/
                    try {
                        btcPrice = JSONTools.convert2DateHashMap(getHTTPSRequest(Constants.API_URL).getJSONObject("bpi"));
                        //progressDialog.setProgress(progressDialog.getCurrentProgress() + 20);
                    } catch (Exception e) {
                        btcPrice = null;
                        Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
                        throw new DataLoaderException("Unable to get data from: " + Constants.API_URL);
                    }
                }
            });
            btcPriceThread.start();
        }

        /*private void completeActivityCreation() {
            if (BitCoinApp.getSharedPreferences().contains(Constants.SHARED_PREFERENCES.APP_VERSION)) {
                if (!BitCoinApp.getSharedPreferences().getString(Constants.SHARED_PREFERENCES.APP_VERSION, "1.0").equals(BitCoinApp.appVersion())) {
                    new MaterialDialog.Builder(DataLoaderScreen.this)
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
                new MaterialDialog.Builder(MainActivity.this)
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

            MainActivity.SectionsPagerAdapter mSectionsPagerAdapter = new MainActivity.SectionsPagerAdapter(getSupportFragmentManager());
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
            ck.checkForUpdates(MainActivity.this, getString(R.string.updateAvailable), getString(R.string.updateDescrip), getString(R.string.updateNow), getString(R.string.updateLater), getString(R.string.updatePage));*/
        //}
    }

    class DataLoaderException extends RuntimeException {
        DataLoaderException(@NonNull String message) {
            super(message);
        }
    }
}
