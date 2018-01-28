package javinator9889.bitcoinpools.FragmentViews;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javinator9889.bitcoinpools.Constants;
import javinator9889.bitcoinpools.JSONTools.JSONTools;
import javinator9889.bitcoinpools.NetTools.net;
import javinator9889.bitcoinpools.R;

/**
 * Created by Javinator9889 on 28/01/2018.
 * Based on https://www.coindesk.com/api/ API
 */

public class Tab2BTCChart extends Fragment {
    private static Map<Date, Float> BTCPRICE = new LinkedHashMap<>();
    private static String API_URL = "https://api.coindesk.com/v1/bpi/historical/close.json";  //?start=2011-09-01&end=2018-01-01";

    public Tab2BTCChart() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab2_btcchart, container, false);
    }

    private void setupValues() {
        //String url = "https://api.coindesk.com/v1/bpi/historical/close.json?start=2011-09-01&end=2018-01-01";
        net httpsResponse = new net();
        httpsResponse.execute(API_URL);
        //Map<Date, Float> newData = new LinkedHashMap<>();
        try {
            BTCPRICE = JSONTools.sortDateByValue(JSONTools.convert2DateHashMap(httpsResponse.get().getJSONObject("bpi")));
        } catch (InterruptedException | ExecutionException | JSONException e) {
            BTCPRICE = null;
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

    private void createLineChart(final LineChart destinationChart) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Entry> values = new ArrayList<>();
                int i = 0;
                LineDataSet lds = new LineDataSet(values, "label");
                Entry entry = new Entry(i, 140000f);
                List<Map.Entry<Date, Float>> entryList = new ArrayList<>(BTCPRICE.entrySet());
                Map.Entry<Date, Float> getEntry;
                int count = 0;
                for (int i = entryList.size() - 1; i >= 0; --i) {
                    getEntry = entryList.get(i);
                    values.add()
                }
            }
        }, 100);
    }
}
