package javinator9889.bitcoinpools.FragmentViews

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.perf.metrics.AddTrace
import javinator9889.bitcoinpools.BitCoinApp
import javinator9889.bitcoinpools.Constants
import javinator9889.bitcoinpools.Constants.SHARED_PREFERENCES.CUSTOM_POOLS
import javinator9889.bitcoinpools.MainActivity
import javinator9889.bitcoinpools.R
import java.util.*

/**
 * Created by Javinator9889 on 28/01/2018. Creates view for main chart (pools chart)
 */

class Tab1PoolsChart : BaseFragment() {
    private var destinationChart: PieChart? = null
    private var tableThread: Thread? = null
    private var mMaximumPoolsToShow: Int = 0
    private var mPoolsText: TextView? = null
    private var md: MaterialDialog? = null

    @AddTrace(name = "onCreateViewForTab1")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val createdView = inflater.inflate(R.layout.poolschart, container, false)

        destinationChart = createdView.findViewById(R.id.chart)
        mPoolsText = createdView.findViewById(R.id.showingNumber)

        val tableLayout = createdView.findViewById<TableLayout>(R.id.poolstable)

        mMaximumPoolsToShow = BitCoinApp.getSharedPreferences().getInt(CUSTOM_POOLS, 10)
        MARKET_PRICE_USD = arguments!!.getFloat("MPU")
        RETRIEVED_DATA = arguments!!.getSerializable("RD") as HashMap<String, Float>

        initT(createdView)
        createPieChart()
        createTable(tableLayout, createdView)
        try {
            tableThread!!.join()
            return createdView
        } catch (e: InterruptedException) {
            e.printStackTrace()
            return null
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val metrics = resources.displayMetrics
        val dpHeight = metrics.heightPixels
        val finalDp = (dpHeight * 0.5).toInt()
        val params = destinationChart!!
                .layoutParams as ConstraintLayout.LayoutParams
        params.height = finalDp
        params.matchConstraintMaxHeight = dpHeight
        destinationChart!!.layoutParams = params
        destinationChart!!.invalidate()

        val duplicatedView = View.inflate(view.context, R.layout.poolschart, null)
        val tableLayout = duplicatedView.findViewById<TableLayout>(R.id.poolstable)
        createTable(tableLayout, duplicatedView)
        try {
            tableThread!!.join()
            md = MaterialDialog.Builder(view.context)
                    .title(R.string.latest_24h_pools_information)
                    .customView(tableLayout, true)
                    .positiveText(R.string.accept)
                    .positiveColor(Color.BLACK)
                    .cancelable(true)
                    .build()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        val container = view.findViewById<ConstraintLayout>(R.id.constraintLayout)
        container.setOnClickListener {
            Toast.makeText(view.context, R.string.show_longer_table, Toast
                    .LENGTH_LONG).show()
        }
        container.setOnLongClickListener {
            md!!.show()
            false
        }
        //        md.show();
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isAdded && isVisibleToUser) {
            MainActivity.MAINACTIVITY_TOOLBAR.title = getString(R.string.BTCP) + MARKET_PRICE_USD
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isAdded && isVisible) {
            MainActivity.MAINACTIVITY_TOOLBAR.title = getString(R.string.BTCP) + MARKET_PRICE_USD
        }
    }

    private fun createPieChart() {
        Handler().postDelayed(// Required for animation
                {
                    Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_CHART)
                    val values = ArrayList<PieEntry>()
                    val entryList = ArrayList<Entry<String, Float>>(
                            RETRIEVED_DATA.entries)
                    var getEntry: Entry<String, Float>
                    var count = 0
                    var i = entryList.size - 1
                    while (i >= 0 && count < mMaximumPoolsToShow) {
                        getEntry = entryList[i]
                        Log.i(Constants.LOG.MATAG, "Accessing at: " + i + " | Key: "
                                + getEntry.key + " | Value: " + getEntry.value)
                        values.add(PieEntry(getEntry.value, getEntry.key))
                        ++count
                        --i
                    }
                    val data = PieDataSet(values, "Latest 24h BTC pools")
                    data.setColors(*ColorTemplate.MATERIAL_COLORS)
                    data.valueTextSize = 10f

                    try {
                        destinationChart!!.data = PieData(data)
                        destinationChart!!.setUsePercentValues(true)
                        destinationChart!!.setEntryLabelColor(ColorTemplate.rgb("#000000"))
                        val description = Description()
                        description.text = getString(R.string.porcent)
                        destinationChart!!.description = description
                        destinationChart!!.legend.isEnabled = false
                        destinationChart!!.dragDecelerationFrictionCoef = 0.95f
                        destinationChart!!.setHardwareAccelerationEnabled(true)
                        destinationChart!!.minimumWidth = 10
                        destinationChart!!.animateY(1400, Easing.EaseInOutQuad)
                    } catch (e: Exception) {
                        Log.e("PieChart", "Error loading PieChart: " + e.message)
                    }
                }, 100)
    }

    private fun createTable(destinationTable: TableLayout, view: View) {
        tableThread = object : Thread() {
            override fun run() {
                Log.d(Constants.LOG.MATAG, Constants.LOG.LOADING_TABLE)
                val entryList = ArrayList<Entry<String, Float>>(
                        RETRIEVED_DATA.entries)
                var getEntry: Entry<String, Float>
                var count = 1

                val fetchTableRow = view.findViewById<TableRow>(R.id.masterRow)
                val firstPool = TextView(view.context)
                val firstBlock = TextView(view.context)

                getEntry = entryList[entryList.size - 1]

                firstPool.setText(getEntry.key)
                firstPool.typeface = Typeface.DEFAULT_BOLD
                firstPool.textSize = 16f

                firstBlock.setText(getEntry.value.toString())
                firstBlock.typeface = Typeface.MONOSPACE
                firstBlock.textSize = 16f

                fetchTableRow.addView(firstPool)
                fetchTableRow.addView(firstBlock)
                var i = entryList.size - 2
                while (i >= 0 && count < mMaximumPoolsToShow) {
                    val poolName = TextView(view.context)
                    val poolBlock = TextView(view.context)
                    val tableRow = TableRow(view.context)

                    getEntry = entryList[i]
                    poolName.setText(getEntry.key)
                    poolBlock.setText(getEntry.value.toString())

                    poolName.typeface = Typeface.DEFAULT_BOLD
                    poolName.textSize = 16f

                    poolBlock.typeface = Typeface.MONOSPACE
                    poolBlock.textSize = 16f

                    tableRow.layoutParams = TABLE_PARAMS
                    tableRow.addView(poolName)
                    tableRow.addView(poolBlock)

                    destinationTable.addView(tableRow)
                    ++count
                    --i
                }
                destinationTable.invalidate()
                mPoolsText!!.text = getString(R.string.number_of_pools_displayed,
                        count,
                        entryList.size)
            }
        }
        tableThread!!.name = "table_thread"
        tableThread!!.start()
    }

    private fun initT(view: View) {
        val masterRow = view.findViewById<TableRow>(R.id.masterRow)
        TABLE_PARAMS = masterRow.layoutParams
    }

    companion object {
        private var RETRIEVED_DATA: Map<String, Float> = LinkedHashMap()
        private var TABLE_PARAMS: ViewGroup.LayoutParams? = null
        private var MARKET_PRICE_USD: Float = 0.toFloat()

        fun newInstance(vararg params: Any): Tab1PoolsChart {
            val args = Bundle()
            args.putFloat("MPU", java.lang.Float.valueOf(params[0].toString()))
            args.putSerializable("RD", params[1] as HashMap<String, Float>)
            val fragment = Tab1PoolsChart()
            fragment.arguments = args
            return fragment
        }
    }
}
