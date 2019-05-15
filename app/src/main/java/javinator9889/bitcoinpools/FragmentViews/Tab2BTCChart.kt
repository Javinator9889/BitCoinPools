package javinator9889.bitcoinpools.FragmentViews

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.firebase.perf.metrics.AddTrace
import javinator9889.bitcoinpools.*
import javinator9889.bitcoinpools.JSONTools.JSONTools
import javinator9889.bitcoinpools.NetTools.net
import org.json.JSONException
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException


/**
 * Created by Javinator9889 on 28/01/2018. Based on https://www.coindesk.com/api/ API
 */

class Tab2BTCChart : BaseFragment(), DatePickerDialog.OnDateSetListener {
    private var cardsContentData: HashMap<String, Float>? = null
    private var year: Int = 0
    private var month: Int = 0
    private var day: Int = 0
    private var writable_month: Int = 0
    private var date_set = false
    private var cardsContents: MutableList<CardsContent>? = null

    private val cachedMap: HashMap<String, String>?
        get() {
            val cache = CacheManaging.newInstance(BitCoinApp.getAppContext())
            try {
                cache.setupFile()
            } catch (e: IOException) {
                Log.e(Constants.LOG.MATAG, "Unable to create cache file")
            }

            return cache.readCache()
        }

    @AddTrace(name = "onCreateViewForTab2")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val dp = this.resources.displayMetrics
        val dpHeight = dp.heightPixels.toFloat()

        val createdView = inflater.inflate(R.layout.bitcoindata, container, false)
        (createdView.findViewById<View>(R.id.datebutton) as Button).setText(R.string.latest30days)
        createdView.findViewById<View>(R.id.datebutton)
                .setOnClickListener { createDialog().show() }

        REQUEST_URL = API_URL

        BTCPRICE = arguments!!.getSerializable("BTCPRICE") as HashMap<Date, Float>
        this.cardsContentData = arguments!!
                .getSerializable("CARDS") as HashMap<String, Float>

        cardsContents = ArrayList()
        val adapter = CardsAdapter(context, cardsContents)
        DESTINATIONLINECHART = createdView.findViewById(R.id.lineChart)
        val recyclerView = createdView.findViewById<RecyclerView>(R.id.recycler_view)

        val layoutManager = GridLayoutManager(createdView.context,
                1)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(GridSpacingItemDecoration(2,
                dpToPx(10), true))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false

        prepareCards()

        val attrs = intArrayOf(R.attr.actionBarSize)
        val a = createdView.context.obtainStyledAttributes(attrs)
        val size = a.getDimensionPixelSize(0, 0)
        a.recycle()
        val FINALDP = ((dpHeight - size) * 0.7).toInt()

        val lp = DESTINATIONLINECHART!!.layoutParams as ConstraintLayout.LayoutParams
        lp.height = FINALDP
        lp.matchConstraintMaxHeight = dpHeight.toInt()
        DESTINATIONLINECHART!!.layoutParams = lp
        DESTINATIONLINECHART!!.invalidate()

        FRAGMENT_CONTEXT = createdView.context
        var longPressInfo: String
        try {
            val cache = CacheManaging.newInstance(createdView.context)
            val date = cache.readCache()!!["date"]
            if (date != null) {
                longPressInfo = getString(R.string.longclick) + "\n" +
                        getString(R.string.comparationDate) + date
            } else
                longPressInfo = getString(R.string.longclick)
            (createdView.findViewById<View>(R.id.longPressInfo) as TextView).text = longPressInfo
            return createdView
        } catch (e: Exception) {
            longPressInfo = getString(R.string.longclick)
            (createdView.findViewById<View>(R.id.longPressInfo) as TextView).text = longPressInfo
            return createdView
        }

    }

    private fun setupValues() {
        val httpsResponse = net()
        httpsResponse.execute(REQUEST_URL)
        try {
            BTCPRICE = JSONTools.sortDateByValue(JSONTools.convert2DateHashMap(
                    httpsResponse.get().getJSONObject("bpi")))
        } catch (e: InterruptedException) {
            BTCPRICE = null
            Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.message)
        } catch (e: ExecutionException) {
            BTCPRICE = null
            Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.message)
        } catch (e: JSONException) {
            BTCPRICE = null
            Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.message)
        } catch (e: NullPointerException) {
            BTCPRICE = null
            Log.e(Constants.LOG.MATAG, Constants.LOG.DATA_ERROR + e.message)
        }

    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && !lineChartCreated) {
            createLineChart(DESTINATIONLINECHART!!, FRAGMENT_CONTEXT!!)
        }
        if (isVisibleToUser)
            MainActivity.MAINACTIVITY_TOOLBAR.title = getString(R.string.btcinfo)
    }

    private fun createLineChart(destinationChart: LineChart,
                                fragmentContext: Context) {
        Handler().postDelayed({
            destinationChart.setDrawGridBackground(false)
            destinationChart.description.isEnabled = false
            destinationChart.setTouchEnabled(true)
            destinationChart.isDragEnabled = true
            destinationChart.setScaleEnabled(true)
            destinationChart.setPinchZoom(true)
            val mv = CustomMarkerView(fragmentContext, R.layout.marker_view)
            mv.chartView = destinationChart
            destinationChart.marker = mv
            val values = ArrayList<Entry>()
            var i = 0
            for (o in BTCPRICE!!.keys) {
                values.add(Entry(i.toFloat(), BTCPRICE!![o]!!))
                ++i
            }
            val lineDataSet: LineDataSet
            if (destinationChart.data != null && destinationChart.data.dataSetCount > 0) {
                lineDataSet = destinationChart.data.getDataSetByIndex(0) as LineDataSet
                lineDataSet.values = values
                destinationChart.data.notifyDataChanged()
                destinationChart.notifyDataSetChanged()
            } else {
                lineDataSet = LineDataSet(values, fragmentContext.getString(R.string.btcprice))
                lineDataSet.setDrawIcons(false)
                lineDataSet.enableDashedLine(10f, 5f, 0f)
                lineDataSet.enableDashedHighlightLine(10f, 5f, 0f)
                lineDataSet.color = Color.BLACK
                lineDataSet.setCircleColor(Color.BLACK)
                lineDataSet.lineWidth = 1f
                lineDataSet.circleRadius = 3f
                lineDataSet.setDrawCircleHole(false)
                lineDataSet.valueTextSize = 9f
                lineDataSet.setDrawFilled(true)
                lineDataSet.formLineWidth = 1f
                lineDataSet.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f),
                        0f)
                lineDataSet.formSize = 15f
                lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
                lineDataSet.fillDrawable = ContextCompat.getDrawable(fragmentContext,
                        R.drawable.fade_red)
                lineDataSet.setDrawCircles(false)
                val dataSets = ArrayList<ILineDataSet>()
                dataSets.add(lineDataSet)
                val data = LineData(dataSets)
                destinationChart.data = data
            }
            destinationChart.axisLeft.valueFormatter = LargeValueFormatter()
            destinationChart.animateX(2500)
            destinationChart.invalidate()
            lineChartCreated = true
        }, 100)
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        var month = month
        val actualDate = Calendar.getInstance()
        actualDate.add(Calendar.DAY_OF_MONTH, -2)
        val dateSet = Calendar.getInstance()
        val dateLimit = Calendar.getInstance()
        dateSet.set(year, month, dayOfMonth)
        dateLimit.set(2010, 6, 17)
        when (dateSet.compareTo(dateLimit)) {
            1 -> when (dateSet.compareTo(actualDate)) {
                -1 -> {
                    this.month = month
                    this.writable_month = ++month
                    this.year = year
                    this.day = dayOfMonth
                }
                else -> {
                    this.month = actualDate.get(Calendar.MONTH)
                    this.writable_month = actualDate.get(Calendar.MONTH) + 1
                    this.year = actualDate.get(Calendar.YEAR)
                    this.day = actualDate.get(Calendar.DAY_OF_MONTH)
                }
            }
            else -> {
                this.month = 6
                this.writable_month = 7
                this.year = 2010
                this.day = 17
            }
        }
        this.date_set = true
        val buttonText = getString(R.string.since) + " " + parseDate()
        (activity!!.findViewById<View>(R.id.datebutton) as Button).text = buttonText
        forceReload()
    }

    private fun parseDate(): String {
        var dateParsed = this.year.toString() + "-"
        if (this.writable_month <= 9)
            dateParsed += "0" + this.writable_month + "-"
        else
            dateParsed += this.writable_month.toString() + "-"
        if (this.day <= 9)
            dateParsed += "0" + this.day
        else
            dateParsed += this.day
        return dateParsed
    }

    fun createDialog(): Dialog {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -2)
        val limitDate = Calendar.getInstance()
        limitDate.set(2010, 6, 17)

        if (!this.date_set) {
            this.year = calendar.get(Calendar.YEAR)
            this.month = calendar.get(Calendar.MONTH)
            this.day = calendar.get(Calendar.DAY_OF_MONTH)
            this.date_set = true
        }

        val dialog = DatePickerDialog(
                activity!!,
                this,
                this.year,
                this.month,
                this.day)

        dialog.datePicker.maxDate = calendar.timeInMillis
        dialog.datePicker.minDate = limitDate.timeInMillis

        return dialog
    }

    fun forceReload() {
        val dateParsed = parseDate()
        REQUEST_URL = "$API_URL?start=$dateParsed&end=" +
                SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        .format(Calendar.getInstance().time)
        setupValues()
        createLineChart(DESTINATIONLINECHART!!, FRAGMENT_CONTEXT!!)
    }

    /**
     * Converting dp to pixel
     */
    private fun dpToPx(dp: Int): Int {
        val r = resources
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                r.displayMetrics))
    }

    private fun prepareCards() {
        val cachedMap = cachedMap
        var market_price: String? = null
        var hash_rate: String? = null
        var difficulty: String? = null
        var blocks_mined: String? = null
        var minutes: String? = null
        var total_fees: String? = null
        var tx: String? = null
        var miners_revenue: String? = null
        if (cachedMap != null) {
            market_price = cachedMap["market_price_usd"]
            hash_rate = cachedMap["hash_rate"]
            difficulty = cachedMap["difficulty"]
            blocks_mined = cachedMap["n_blocks_mined"]
            minutes = cachedMap["minutes_between_blocks"]
            total_fees = cachedMap["total_fees_btc"]
            tx = cachedMap["n_tx"]
            miners_revenue = cachedMap["miners_revenue_usd"]
        }
        val df = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))
        cardsContents!!.add(CardsContent(getString(R.string.market_price),
                "$" + df.format(this.cardsContentData!!["market_price_usd"]),
                market_price))
        cardsContents!!.add(CardsContent(getString(R.string.hash_rate),
                df.format(this.cardsContentData!!["hash_rate"]) + " GH/s",
                hash_rate))
        cardsContents!!.add(CardsContent(getString(R.string.difficulty),
                df.format(this.cardsContentData!!["difficulty"]),
                difficulty))
        cardsContents!!.add(CardsContent(getString(R.string.min_blocks),
                df.format((this.cardsContentData!!["n_blocks_mined"]!! / 10).toDouble())
                        + " " + getString(R.string.blocks_name),
                blocks_mined))
        cardsContents!!.add(CardsContent(getString(R.string.minutes_blocks),
                df.format(this.cardsContentData!!["minutes_between_blocks"])
                        + " " + getString(R.string.minutes_name),
                minutes))
        cardsContents!!.add(CardsContent(getString(R.string.total_fees),
                df.format((this.cardsContentData!!["total_fees_btc"]!! / 10000000).toDouble()) + " BTC",
                total_fees))
        cardsContents!!.add(CardsContent(getString(R.string.total_trans),
                df.format(this.cardsContentData!!["n_tx"]),
                tx))
        cardsContents!!.add(CardsContent(getString(R.string.min_benefit),
                "$" + df.format((this.cardsContentData!!["miners_revenue_usd"]!! / 100).toDouble()),
                miners_revenue))
    }

    inner class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration()

    companion object {
        private val API_URL = "https://api.coindesk.com/v1/bpi/historical/close.json"
        private val STATS_URL = "https://api.blockchain.info/stats"
        private var BTCPRICE: Map<Date, Float>? = LinkedHashMap()
        private var REQUEST_URL: String? = null
        private var DESTINATIONLINECHART: LineChart? = null
        @SuppressLint("StaticFieldLeak")
        private var FRAGMENT_CONTEXT: Context? = null
        private var lineChartCreated = false

        fun newInstance(vararg params: Any): Tab2BTCChart {
            val args = Bundle()
            args.putSerializable("CARDS", params[0] as HashMap<String, Float>)
            args.putSerializable("BTCPRICE", params[1] as HashMap<Date, Float>)
            val fragment = Tab2BTCChart()
            fragment.arguments = args
            return fragment
        }

        fun setLineChartCreated() {
            lineChartCreated = false
        }
    }
}
