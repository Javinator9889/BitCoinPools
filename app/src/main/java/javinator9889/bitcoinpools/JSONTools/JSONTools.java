package javinator9889.bitcoinpools.JSONTools;

import android.annotation.SuppressLint;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Javinator9889 on 20/12/2017.
 * Based on: https://www.mkyong.com/java/how-to-sort-a-map-in-java/
 */

public class JSONTools {
    public static Map<String, Float> sortByValue(Map<String, Float> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Float>> list =
                new LinkedList<>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, Float>>() {
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Float> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public static HashMap<String, Float> convert2HashMap(JSONObject object) {
        HashMap<String, Float> hReturn = new HashMap<>();
        Iterator<String> iterator = object.keys();
        try {
            while (iterator.hasNext()) {
                String key = iterator.next();
                hReturn.put(key, (float) object.getDouble(key));
            }
            return hReturn;
        } catch (JSONException e) {
            return null;
        }
    }

    public static HashMap<Date, Float> convert2DateHashMap(JSONObject object) {
        HashMap<Date, Float> hReturn = new HashMap<>();
        Iterator<String> iterator = object.keys();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            while (iterator.hasNext()) {
                String key = iterator.next();
                hReturn.put(dateFormat.parse(key), (float) object.getDouble(key));
            }
            return hReturn;
        } catch (JSONException | ParseException e) {
            return null;
        }
    }

    public static Map<Date, Float> sortDateByValue(Map<Date, Float> unsortMap) {

//        DateFormat df = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        Map<Date, Float> m1 = new TreeMap<>(unsortMap);
        Map<Date, Float> returnMap = new LinkedHashMap<>();
        for (Map.Entry<Date, Float> entry : m1.entrySet()) {
            returnMap.put(entry.getKey(), entry.getValue());
        }
        return returnMap;
    }
}
