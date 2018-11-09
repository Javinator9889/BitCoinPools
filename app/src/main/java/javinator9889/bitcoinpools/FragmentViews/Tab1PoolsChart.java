package javinator9889.bitcoinpools.FragmentViews;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
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
import com.google.firebase.perf.metrics.AddTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import javinator9889.bitcoinpools.Constants;
import javinator9889.bitcoinpools.MainActivity;
import javinator9889.bitcoinpools.R;

/**
 * Created by Javinator9889 on 28/01/2018.
 * Creates view for main chart (pools chart)
 */

public class Tab1PoolsChart extends Fragment {
    private static Map<String, Float> RETRIEVED_DATA = new LinkedHashMap<>();
    private static ViewGroup.LayoutParams TABLE_PARAMS;
    private static float MARKET_PRICE_USD;
    private PieChart destinationChart;
    private Thread tableThread;

    public Tab1PoolsChart() {
    }

    @SuppressWarnings("unchecked")
    public static Tab1PoolsChart newInstance(Object... params) {
        Bundle args = new Bundle();
        args.putFloat("MPU", Float.valueOf(String.valueOf(params[0])));
        args.putSerializable("RD", (HashMap<String, Float>) params[1]);
        Tab1PoolsChart fragment = new Tab1PoolsChart();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("unchecked")
    @Override
    @AddTrace(name = "onCreateViewForTab1")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View createdView = inflater.inflate(R.layout.poolschart, container, false);

        destinationChart = createdView.findViewById(R.id.chart);
        final TableLayout tableLayout = createdView.findViewById(R.id.poolstable);
        MARKET_PRICE_USD = getArguments().getFloat("MPU");
        RETRIEVED_DATA = (HashMap<String, Float>) getArguments().getSerializable("RD");

        initT(createdView);
        createPieChart();
        createTable(tableLayout, createdView);
        try {
            tableThread.join();
            return createdView;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        createPieChart();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int dpHeight = metrics.heightPixels;
        int finalDp = (int) (dpHeight * 0.5);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) destinationChart
                .getLayoutParams();
        params.height = finalDp;
        params.matchConstraintMaxHeight = dpHeight;
        destinationChart.setLayoutParams(params);
        destinationChart.invalidate();
    }

    /*@SuppressWarnings("unchecked")
    @Override
    @AddTrace(name = "onCreateViewForTab1")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final PieChart chart = view.findViewById(R.id.chart);
        final TableLayout tableLayout = view.findViewById(R.id.poolstable);
        MARKET_PRICE_USD = getArguments().getFloat("MPU");
        RETRIEVED_DATA = (HashMap<String, Float>) getArguments().getSerializable("RD");

        initT(view);
        createPieChart(chart);
        createTable(tableLayout, view);
        try {
            tableThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onViewCreated(view, savedInstanceState);
    }*/

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

    private void createPieChart() {
        new Handler().postDelayed(new Runnable() {  // Required for animation
            @Override
            public void run() {
                Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_CHART);
                List<PieEntry> values = new ArrayList<>();
                List<Map.Entry<String, Float>> entryList = new ArrayList<>(
                        RETRIEVED_DATA.entrySet());
                Map.Entry<String, Float> getEntry;
                int count = 0;
                for (int i = entryList.size() - 1; (i >= 0) && (count < 10); --i) {
                    getEntry = entryList.get(i);
                    Log.i(Constants.LOG.MATAG, "Accessing at: " + i + " | Key: "
                            + getEntry.getKey() + " | Value: " + getEntry.getValue());
                    values.add(new PieEntry(getEntry.getValue(), getEntry.getKey()));
                    ++count;
                }
                PieDataSet data = new PieDataSet(values, "Latest 24h BTC pools");
                data.setColors(ColorTemplate.MATERIAL_COLORS);
                data.setValueTextSize(10f);

                try {
                    destinationChart.setData(new PieData(data));
                    destinationChart.setUsePercentValues(true);
                    destinationChart.setEntryLabelColor(ColorTemplate.rgb("#000000"));
                    Description description = new Description();
                    description.setText(getString(R.string.porcent));
                    destinationChart.setDescription(description);
                    destinationChart.getLegend().setEnabled(false);
                    destinationChart.setDragDecelerationFrictionCoef(0.95f);
                    destinationChart.setHardwareAccelerationEnabled(true);
                    destinationChart.setMinimumWidth(10);
//                    destinationChart.setDrawCenterText(false);
                    destinationChart.animateY(1400, Easing.EaseInOutQuad);
//                    destinationChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
                } catch (Exception e) {
                    Log.e("PieChart", "Error loading PieChart: " + e.getMessage());
                }
            }
        }, 100);
//        destinationChart.invalidate();
    }

    private void createTable(final TableLayout destinationTable, final View view) {
        tableThread = new Thread() {
            public void run() {
                Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_TABLE);
                List<Map.Entry<String, Float>> entryList = new ArrayList<>(
                        RETRIEVED_DATA.entrySet());
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
        tableThread.setName("table_thread");
        tableThread.start();
    }

    private void initT(View view) {
        TableRow masterRow = view.findViewById(R.id.masterRow);
        TABLE_PARAMS = masterRow.getLayoutParams();
    }
}
