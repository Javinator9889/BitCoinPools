package javinator9889.bitcoinpools.JSONTools

import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Javinator9889 on 20/12/2017. Based on: https://www.mkyong.com/java/how-to-sort-a-map-in-java/
 */

object JSONTools {
    fun sortByValue(unsortMap: HashMap<String, Float>): HashMap<String, Float> {

        // 1. Convert Map to List of Map
        val list = LinkedList<Entry<String, Float>>(unsortMap.entries)

        Collections.sort<Entry<String, Float>>(list) { o1, o2 -> o1.value.compareTo(o2.value) }

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        val sortedMap = LinkedHashMap<String, Float>()
        for (entry in list) {
            sortedMap[entry.key] = entry.value
        }
        return sortedMap
    }

    fun convert2HashMap(`object`: JSONObject): HashMap<String, Float>? {
        val hReturn = HashMap<String, Float>()
        val iterator = `object`.keys()
        try {
            while (iterator.hasNext()) {
                val key = iterator.next()
                hReturn[key] = `object`.getDouble(key).toFloat()
            }
            return hReturn
        } catch (e: JSONException) {
            return null
        }

    }

    fun convert2DateHashMap(`object`: JSONObject): HashMap<Date, Float>? {
        val hReturn = HashMap<Date, Float>()
        val iterator = `object`.keys()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            while (iterator.hasNext()) {
                val key = iterator.next()
                hReturn[dateFormat.parse(key)] = `object`.getDouble(key).toFloat()
            }
            return hReturn
        } catch (e: JSONException) {
            return null
        } catch (e: ParseException) {
            return null
        }

    }

    fun sortDateByValue(unsortMap: HashMap<Date, Float>): HashMap<Date, Float> {
        val m1 = TreeMap(unsortMap)
        val returnMap = LinkedHashMap<Date, Float>()
        for ((key, value) in m1) {
            returnMap[key] = value
        }
        return returnMap
    }
}
