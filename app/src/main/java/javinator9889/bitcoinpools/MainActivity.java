package javinator9889.bitcoinpools;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javinator9889.bitcoinpools.AppUpdaterManager.CheckUpdates;
import javinator9889.bitcoinpools.JSONTools.JSONTools;
import javinator9889.bitcoinpools.NetTools.net;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static Map<String, Float> RETRIEVED_DATA = new LinkedHashMap<>();
    private static float MARKET_PRICE_USD;
    private static ViewGroup.LayoutParams TABLE_PARAMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_MAINVIEW);
        setContentView(R.layout.activity_main);

        Log.d(Constants.LOG.MATAG, Constants.LOG.INIT_VALUES);
        checkPermissions();
        initMPU();
        initRD();
        initT();
        CheckUpdates ck = new CheckUpdates(Constants.GITHUB_USER, Constants.GITHUB_REPO);
        setTitle(getString(R.string.BTCP) + MARKET_PRICE_USD);

        final FloatingActionsMenu mainButton = (FloatingActionsMenu) findViewById(R.id.menu_fab);
        final FloatingActionButton licenseButton = (FloatingActionButton) findViewById(R.id.license);
        final FloatingActionButton closeButton = (FloatingActionButton) findViewById(R.id.close);
        final FloatingActionButton settingsButton = (FloatingActionButton) findViewById(R.id.settings);
        final FloatingActionButton refreshButton = (FloatingActionButton) findViewById(R.id.update);
        final PieChart chart = (PieChart) findViewById(R.id.chart);

        Log.d(Constants.LOG.MATAG, Constants.LOG.CREATING_CHART);
        createPieChart(chart);
        chart.invalidate();
        createTable((TableLayout) findViewById(R.id.poolstable));
        mainButton.bringToFront();
        mainButton.invalidate();

        Log.d(Constants.LOG.MATAG, Constants.LOG.LISTENING);
        licenseButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        refreshButton.setOnClickListener(this);

        ck.checkForUpdates(this, getString(R.string.updateAvailable), getString(R.string.updateDescrip), getString(R.string.updateNow), getString(R.string.updateLater), getString(R.string.updatePage));
    }

    private void refresh() {
        Intent intentMain = new Intent(MainActivity.this, MainActivity.class);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intentMain);
        MainActivity.this.finish();
    }

    private void initMPU() {
        net market = new net();
        market.execute("https://api.blockchain.info/stats");
        try {
            MARKET_PRICE_USD = round((float) market.get().getDouble("market_price_usd"), 2);
        } catch (InterruptedException | ExecutionException | JSONException e) {
            Log.e(Constants.LOG.MATAG, Constants.LOG.MARKET_PRICE_ERROR + e.getMessage());
            MARKET_PRICE_USD = 0;
        }
    }

    private void initRD() {
        int days = BitCoinApp.getSharedPreferences().getInt(Constants.SHARED_PREFERENCES.DAYS_TO_CHECK, 1);
        String url = "https://api.blockchain.info/pools?timespan=" + days + "days";
        net httpsResponse = new net();
        httpsResponse.execute(url);
        try {
            RETRIEVED_DATA = JSONTools.sortByValue(JSONTools.convert2HashMap(httpsResponse.get()));
        } catch (InterruptedException | ExecutionException e) {
            RETRIEVED_DATA = null;
            Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.getMessage());
        }
    }

    private void initT() {
        TableRow masterRow = (TableRow) findViewById(R.id.masterRow);
        TABLE_PARAMS = masterRow.getLayoutParams();
    }

    private void createPieChart(PieChart destinationChart) {
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

    private void createTable(TableLayout destinationTable) {
        List<Map.Entry<String, Float>> entryList = new ArrayList<>(RETRIEVED_DATA.entrySet());
        Map.Entry<String, Float> getEntry;
        int count = 1;

        TableRow fetchTableRow = findViewById(R.id.masterRow);
        TextView firstPool = new TextView(this);
        TextView firstBlock = new TextView(this);

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
            TextView poolName = new TextView(this);
            TextView poolBlock = new TextView(this);
            TableRow tableRow = new TableRow(this);

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
                Intent intentLicense = new Intent(this, License.class);
                startActivity(intentLicense);
                break;
            case R.id.settings:
                Intent intentSettings = new Intent(this, SpinnerActivity.class);
                startActivity(intentSettings);
                MainActivity.this.finish();
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
}

