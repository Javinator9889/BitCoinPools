package javinator9889.bitcoinpools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javinator9889.bitcoinpools.JSONTools.JSONTools;
import javinator9889.bitcoinpools.NetTools.net;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static Map<String, Float> RETRIEVED_DATA = new LinkedHashMap<>();
    private static final String FILENAME = "usrPref.dat";
    private static float MARKET_PRICE_USD;
    private static ViewGroup.LayoutParams TABLE_PARAMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initWHD();
        initFile();
        initMPU();
        initRD();
        initT();
        setTitle(getString(R.string.BTCP) + MARKET_PRICE_USD);

        final FloatingActionsMenu mainButton = (FloatingActionsMenu) findViewById(R.id.menu_fab);
        final FloatingActionButton licenseButton = (FloatingActionButton) findViewById(R.id.license);
        final FloatingActionButton closeButton = (FloatingActionButton) findViewById(R.id.close);
        final FloatingActionButton settingsButton = (FloatingActionButton) findViewById(R.id.settings);
        final FloatingActionButton refreshButton = (FloatingActionButton) findViewById(R.id.update);
        final PieChart chart = (PieChart) findViewById(R.id.chart);

        createPieChart(chart);
        chart.invalidate();
        createTable((TableLayout) findViewById(R.id.poolstable));
        mainButton.bringToFront();
        mainButton.invalidate();

        licenseButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        refreshButton.setOnClickListener(this);
        //ReloadPage((SwipeRefreshLayout) findViewById(R.id.swipeReload));
    }

    private void refresh() {
        Intent intentMain = new Intent(MainActivity.this, MainActivity.class);
        intentMain.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intentMain);
        MainActivity.this.finish();
    }

    private void initFile() {
        File file = new File(this.getFilesDir(), FILENAME);
        try {
            file.createNewFile();
            file.setWritable(true);
            file.setReadable(true);
        } catch (IOException e) {
            System.out.println("There was an error while trying to create new file. Full trace:");
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            FileInputStream getInput = openFileInput(FILENAME);
            if (getInput.read() == -1) {
                try {
                    FileOutputStream setOutput = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    setOutput.write(1);
                    setOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            getInput.close();
        } catch (IOException m) {
            m.printStackTrace();
        }
    }

    private void initMPU() {
        net market = new net();
        market.execute("https://api.blockchain.info/stats");
        try {
            MARKET_PRICE_USD = round((float) market.get().getDouble("market_price_usd"), 2);
        } catch (InterruptedException | ExecutionException | JSONException e) {
            MARKET_PRICE_USD = 0;
        }
    }

    private void initRD() {
        int days = 1;
        try {
            days = openFileInput(FILENAME).read();
            openFileInput(FILENAME).close();
        } catch (IOException e) {
            e.printStackTrace();
            super.finish();
        }
        /*if (days <= 0)
            days = 1;*/
        String url = "https://api.blockchain.info/pools?timespan="+days+"days";
        System.out.println(url);
        net httpsResponse = new net();
        httpsResponse.execute(url);
        try {
            RETRIEVED_DATA = JSONTools.sortByValue(JSONTools.convert2HashMap(httpsResponse.get()));
        } catch (InterruptedException | ExecutionException e) {
            RETRIEVED_DATA = null;
        }
    }

    private void initT() {
        TableRow masterRow = (TableRow)findViewById(R.id.masterRow);
        TABLE_PARAMS = masterRow.getLayoutParams();
    }

    private void createPieChart(PieChart destinationChart) {
        List<PieEntry> values = new ArrayList<>();
        System.out.println(RETRIEVED_DATA.toString());
        List<Map.Entry<String, Float>> entryList = new ArrayList<>(RETRIEVED_DATA.entrySet());
        Map.Entry<String, Float> getEntry;
        int count = 0;
        for (int i = entryList.size() - 1; (i >= 0) && (count < 10); --i) {
            getEntry = entryList.get(i);
            System.out.println("Accessing at: " + i + " | Key: " + getEntry.getKey() + " | Value: " + getEntry.getValue());
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
        //noneDescription.setText("");
        //destinationChart.setDescription(noneDescription);
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
    private static float round(float d, int decimalPlace) {
        return BigDecimal.valueOf(d).setScale(decimalPlace,BigDecimal.ROUND_HALF_UP).floatValue();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close:
                this.onBackPressed();
                break;
            case R.id.license:
                Intent intentLicense = new Intent(this, License.class);
                //intentLicense.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intentLicense);
                //MainActivity.this.finish();
                break;
            case R.id.settings:
                Intent intentSettings = new Intent(this, SpinnerActivity.class);
                startActivity(intentSettings);
                MainActivity.this.finish();
                //refresh();
                break;
            case R.id.update:
                refresh();
                Toast.makeText(this, R.string.updated, Toast.LENGTH_LONG).show();
                break;
            default:
                System.exit(1);
        }
    }
}

