package javinator9889.bitcoinpools;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import javinator9889.bitcoinpools.JSONTools.JSONTools;

import static javinator9889.bitcoinpools.MainActivity.round;

/**
 * Created by Javinator9889 on 04/03/2018.
 * Based on: https://stackoverflow.com/questions/10115403/progressdialog-while-load-activity
 */

public class DataLoaderScreen extends AppCompatActivity {
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
            new DataLoader().execute();
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.noConnectionTitle)
                    .content(R.string.noConnectionDesc)
                    .cancelable(false)
                    .positiveText(R.string.accept)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog,
                                            @NonNull DialogAction which) {
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
        private Thread.UncaughtExceptionHandler threadExceptions = new Thread
                .UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                isAnyExceptionThrown = true;
                Log.e("DataLoaderScreen", "Exception on thread: " + t.getName()
                        + " | Message: " + e.getMessage());
            }
        };
        private boolean isAnyExceptionThrown = false;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result && !isAnyExceptionThrown) {
                Intent activityMainIntent = new Intent(DataLoaderScreen.this,
                        MainActivity.class);
                activityMainIntent.putExtra("MPU", mpu);
                activityMainIntent.putExtra("RD", retrievedData);
                activityMainIntent.putExtra("CARDS", cardsData);
                activityMainIntent.putExtra("BTCPRICE", btcPrice);
                startActivity(activityMainIntent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            } else {
                new MaterialDialog.Builder(DataLoaderScreen.this)
                        .positiveText(R.string.accept)
                        .cancelable(false)
                        .title(R.string.errorLoading)
                        .content(R.string.errorLoadingDescription,
                                true)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog,
                                                @NonNull DialogAction which) {
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
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();
            return new JSONObject(response.toString());
        }

        private void getBitCoinMarketPrice() throws DataLoaderException {
            marketPriceThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_MPU);
                    try {
                        mpu = round((float) getHTTPSRequest(Constants.STATS_URL)
                                .getDouble(Constants.MARKET_NAME), 2);
                    } catch (Exception e) {
                        Log.e(Constants.LOG.MATAG,
                                Constants.LOG.MARKET_PRICE_ERROR + e.getMessage());
                        mpu = -1;
                        throw new DataLoaderException(
                                "Failed to get data from: " + Constants.STATS_URL);
                    }
                }
            });
            marketPriceThread.setUncaughtExceptionHandler(threadExceptions);
            marketPriceThread.setName("MarketPriceThread");
            marketPriceThread.start();
        }

        private void getPoolsData() {
            poolsDataThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    int days = BitCoinApp.getSharedPreferences()
                            .getInt(Constants.SHARED_PREFERENCES.DAYS_TO_CHECK, 1);
                    Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_RD);
                    String url = Constants.POOLS_URL + days + "days";
                    try {
                        retrievedData = JSONTools.convert2HashMap(getHTTPSRequest(url));
                    } catch (Exception e) {
                        retrievedData = null;
                        Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
                        throw new DataLoaderException("Unable to get data from: " + url);
                    }
                }
            });
            poolsDataThread.setUncaughtExceptionHandler(threadExceptions);
            poolsDataThread.setName("PoolsDataThread");
            poolsDataThread.start();
        }

        private void getCardsData() {
            cardsDataThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cardsData = JSONTools.convert2HashMap(getHTTPSRequest(Constants.STATS_URL));
                    } catch (Exception e) {
                        cardsData = null;
                        Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
                        throw new DataLoaderException(
                                "Unable to get data from: " + Constants.STATS_URL);
                    }
                }
            });
            cardsDataThread.setUncaughtExceptionHandler(threadExceptions);
            cardsDataThread.setName("CardsDataThread");
            cardsDataThread.start();
        }

        private void getBitCoinPriceHistory() {
            btcPriceThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        btcPrice = JSONTools.convert2DateHashMap(getHTTPSRequest(Constants.API_URL)
                                .getJSONObject("bpi"));
                    } catch (Exception e) {
                        btcPrice = null;
                        Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
                        throw new DataLoaderException(
                                "Unable to get data from: " + Constants.API_URL);
                    }
                }
            });
            btcPriceThread.setUncaughtExceptionHandler(threadExceptions);
            btcPriceThread.setName("BtcPriceThread");
            btcPriceThread.start();
        }
    }

    class DataLoaderException extends RuntimeException {
        DataLoaderException(@NonNull String message) {
            super(message);
        }
    }
}
