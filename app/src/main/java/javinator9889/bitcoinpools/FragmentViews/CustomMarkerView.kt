package javinator9889.bitcoinpools.FragmentViews

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView

import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils

import javinator9889.bitcoinpools.R

/**
 * Created by Javinator9889 on 29/01/2018. Based on: https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/custom/MyMarkerView.java
 */

@SuppressLint("ViewConstructor")
class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {

    private val tvContent: TextView

    init {

        tvContent = findViewById(R.id.tvContent)
    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @SuppressLint("SetTextI18n")
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e is CandleEntry) {
            val ce = e as CandleEntry?
            tvContent.text = "" + Utils.formatNumber(ce!!.high, 0,
                    true)
        } else {
            tvContent.text = "" + Utils.formatNumber(e!!.y, 0, true)
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }
}
