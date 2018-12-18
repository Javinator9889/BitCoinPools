package javinator9889.bitcoinpools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.perf.metrics.AddTrace;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import javinator9889.bitcoinpools.JSONTools.JSONTools;
import javinator9889.bitcoinpools.NetTools.net;

import static javinator9889.bitcoinpools.MainActivity.round;

/**
 * Created by Javinator9889 on 04/03/2018. Based on: https://stackoverflow.com/questions/10115403/progressdialog-while-load-activity
 */

public class DataLoaderScreen extends BaseActivity {
    public static AppCompatActivity dataLoaderScreenActivity;

    private float mpu;
    private HashMap<String, Float> retrievedData;
    private HashMap<String, Float> cardsData;
    private HashMap<Date, Float> btcPrice;
    private Context mContext;

    @Override
    @AddTrace(name = "getApplicationValues")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataLoaderScreenActivity = this;
        setContentView(R.layout.activity_loading);
        mContext = getApplicationContext();
        if (BitCoinApp.isOnline()) {
            Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_MAINVIEW);
            new DataLoader().execute();
        } else {
            try {
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
            } catch (Exception e) {
                // MaterialDialog lib doesn't provide functionality to catch its own exception
                // "DialogException" so we need to catch a global generally exception and cancel
                // app execution. This happens because the application is trying to show a dialog
                // after the app has been closed (there is no activity).
                Log.e("MaterialDialog", "Not possible to show dialog - maybe the app is" +
                        " closed. Full trace: " + e.getMessage());
                try {
                    this.finish();
                } catch (Exception activityFinishException) {
                    // Maybe closing the activity when execution failed throws an exception if
                    // there is no possibility to close it
                    Log.e("FinishException", "Impossible to finish the activity. Maybe " +
                            "it is finished yet. More info: "
                            + activityFinishException.getMessage());
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    class DataLoader extends AsyncTask<Void, Void, Boolean> {
        private Thread marketPriceThread;
        private Thread poolsDataThread;
        private Thread cardsDataThread;
        private Thread btcPriceThread;
        private boolean isAnyExceptionThrown = false;
        private boolean areHostsReachable = true;
        private Thread.UncaughtExceptionHandler threadExceptions = new Thread
                .UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                isAnyExceptionThrown = true;
                Log.e("DataLoaderScreen", "Exception on thread: " + t.getName()
                        + " | Message: " + e.getMessage());
                FirebaseCrash.log("DataLoaderScreen. Exception on thread: " + t.getName()
                        + " | Message: " + e.getMessage());
                if (e instanceof HostNonReachableException) {
                    areHostsReachable = false;
                }
            }
        };

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
            } else if (!areHostsReachable) {
                try {
                    new MaterialDialog.Builder(mContext)
                            .title(R.string.host_not_available_title)
                            .content(R.string.host_not_available_desc)
                            .cancelable(false)
                            .positiveText(R.string.accept)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog dialog, DialogAction which) {
                                    onBackPressed();
                                }
                            })
                            .build()
                            .show();
                } catch (Exception e) {
                    try {
                        finish();
                    } catch (Exception ignored) {
                    }
                }
            } else {
                if (BitCoinApp.isOnline()) {
                    Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_MAINVIEW);
                    new DataLoader().execute();
                } else {
                    try {
                        new MaterialDialog.Builder(DataLoaderScreen.this)
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
                    } catch (Exception e) {
                        // MaterialDialog lib doesn't provide functionality to catch its own
                        // exception
                        // "DialogException" so we need to catch a global generally exception and
                        // cancel
                        // app execution. This happens because the application is trying to show
                        // a dialog
                        // after the app has been closed (there is no activity).
                        Log.e("MaterialDialog",
                                "Not possible to show dialog - maybe the app is" +
                                        " closed. Full trace: " + e.getMessage());
                        try {
                            DataLoaderScreen.this.finish();
                        } catch (Exception activityFinishException) {
                            // Maybe closing the activity when execution failed throws an exception
                            // if there is no possibility to close it
                            Log.e("FinishException",
                                    "Impossible to finish the activity. Maybe it is finished" +
                                            " yet. More info: "
                                            + activityFinishException.getMessage());
                        }
                    }
                }
                Log.e("DataLoaderScreen", "An exception was thrown. Trying to obtain " +
                        "data again");
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
                isAnyExceptionThrown = true;
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
                    if (!net.isHostReachable(Constants.STATS_URL, mContext))
                        throw new HostNonReachableException(String.format("The URL \"%s\" is not " +
                                "reachable", Constants.STATS_URL));
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
                    if (!net.isHostReachable(url, mContext))
                        throw new HostNonReachableException(String.format("The URL \"%s\" is not " +
                                "reachable", url));
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
                    if (!net.isHostReachable(Constants.STATS_URL, mContext))
                        throw new HostNonReachableException(String.format("The URL \"%s\" is not " +
                                "reachable", Constants.STATS_URL));
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
                    if (!net.isHostReachable(Constants.API_URL, mContext))
                        throw new HostNonReachableException(String.format("The URL \"%s\" is not " +
                                "reachable", Constants.API_URL));
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

    class HostNonReachableException extends RuntimeException {
        HostNonReachableException(String message) {super(message);}
    }
}
