package javinator9889.bitcoinpools.FragmentViews;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javinator9889.bitcoinpools.BitCoinApp;
import javinator9889.bitcoinpools.Constants;
import javinator9889.bitcoinpools.JSONTools.JSONTools;
import javinator9889.bitcoinpools.MainActivity;
import javinator9889.bitcoinpools.NetTools.net;
import javinator9889.bitcoinpools.R;

import static javinator9889.bitcoinpools.MainActivity.round;

/**
 * Created by Javinator9889 on 28/01/2018.
 * Creates view for main chart (pools chart)
 */

public class Tab1PoolsChart extends Fragment {
    private static Map<String, Float> RETRIEVED_DATA = new LinkedHashMap<>();
    private static ViewGroup.LayoutParams TABLE_PARAMS;
    private static float MARKET_PRICE_USD;
    private Thread rdThread;
    private Thread tableThread;
    private Thread mpuThread;

    public Tab1PoolsChart() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View createdView = inflater.inflate(R.layout.poolschart, container, false);

        final PieChart chart = createdView.findViewById(R.id.chart);
        final TableLayout tableLayout = createdView.findViewById(R.id.poolstable);

        initRD();
        initT(createdView);
        initMPU();

        createPieChart(chart);
        createTable(tableLayout, createdView);

        try {
            tableThread.join();
            mpuThread.join();
            return createdView;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isAdded() && isVisibleToUser) {
            MainActivity.MAINACTIVITY_TOOLBAR.setTitle(getString(R.string.BTCP) + MARKET_PRICE_USD);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isAdded() && isVisible()) {
            MainActivity.MAINACTIVITY_TOOLBAR.setTitle(getString(R.string.BTCP) + MARKET_PRICE_USD);
        }
    }

    private void createPieChart(final PieChart destinationChart) {
        new Handler().postDelayed(new Runnable() {  // Required for animation
            @Override
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
                destinationChart.setDragDecelerationFrictionCoef(0.95f);
                destinationChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
                destinationChart.invalidate();
            }
        }, 100);
    }

    private void createTable(final TableLayout destinationTable, final View view) {
        tableThread = new Thread() {
            public void run() {
                Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_TABLE);
                List<Map.Entry<String, Float>> entryList = new ArrayList<>(RETRIEVED_DATA.entrySet());
                Map.Entry<String, Float> getEntry;
                int count = 1;

                TableRow fetchTableRow = view.findViewById(R.id.masterRow);
                TextView firstPool = new TextView(view.getContext());
                TextView firstBlock = new TextView(view.getContext());

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
                    TextView poolName = new TextView(view.getContext());
                    TextView poolBlock = new TextView(view.getContext());
                    TableRow tableRow = new TableRow(view.getContext());

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

    private void initT(View view) {
        TableRow masterRow = view.findViewById(R.id.masterRow);
        TABLE_PARAMS = masterRow.getLayoutParams();
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
}
