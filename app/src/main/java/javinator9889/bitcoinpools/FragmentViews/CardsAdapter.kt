package javinator9889.bitcoinpools.FragmentViews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import javinator9889.bitcoinpools.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * Created by Javinator9889 on 31/01/2018. Based on: https://www.androidhive.info/2016/05/android-working-with-card-view-and-recycler-view/
 */

class CardsAdapter internal constructor(private val context: Context, private val btcData: List<CardsContent>) : RecyclerView.Adapter<CardsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.bitcoin_card,
                parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder,
                                  @SuppressLint("RecyclerView") position: Int) {
        val content = btcData[position]
        holder.title.text = content.title
        holder.body.text = content.body
        val decimalFormat = DecimalFormat("#.##",
                DecimalFormatSymbols(Locale.US))
        if (content.oldData == null)
            holder.oldData.text = ""
        else {
            when (position) {
                0 -> {
                    val newPrice = java.lang.Float.parseFloat(
                            content.body.replace("[^\\d.]".toRegex(), ""))
                    val oldPrice = java.lang.Float.parseFloat(
                            decimalFormat.format(java.lang.Float.parseFloat(content.oldData).toDouble()))
                    val pricePercentage = percentageCalculator(newPrice, oldPrice)
                    when (java.lang.Float.compare(pricePercentage, 0f)) {
                        0 -> holder.oldData.text = "0%"
                        1 -> {
                            val positiveText = "+" + decimalFormat.format(pricePercentage.toDouble()) + "%"
                            holder.oldData.text = positiveText
                            holder.oldData.setTextColor(Color.GREEN)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0)
                        }
                        -1 -> {
                            val negativeText = decimalFormat.format(pricePercentage.toDouble()) + "%"
                            holder.oldData.text = negativeText
                            holder.oldData.setTextColor(Color.RED)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0)
                        }
                    }
                }
                1 -> {
                    val newPower = java.lang.Float.parseFloat(
                            content.body.replace("[^\\d.]".toRegex(), ""))
                    val oldPower = java.lang.Float.parseFloat(
                            decimalFormat.format(java.lang.Float.parseFloat(content.oldData).toDouble()))
                    val powerPercentage = percentageCalculator(newPower, oldPower)
                    when (java.lang.Float.compare(powerPercentage, 0f)) {
                        0 -> holder.oldData.text = "0%"
                        1 -> {
                            val positiveText = "+" + decimalFormat.format(powerPercentage.toDouble()) + "%"
                            holder.oldData.text = positiveText
                            holder.oldData.setTextColor(Color.GREEN)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0)
                        }
                        -1 -> {
                            val negativeText = decimalFormat.format(powerPercentage.toDouble()) + "%"
                            holder.oldData.text = negativeText
                            holder.oldData.setTextColor(Color.RED)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0)
                        }
                    }
                }
                2 -> {
                    val newDifficulty = java.lang.Float.parseFloat(
                            content.body.replace("[^\\d.]".toRegex(), ""))
                    val oldDifficulty = java.lang.Float.parseFloat(
                            decimalFormat.format(java.lang.Float.parseFloat(content.oldData).toDouble()))
                    val difficultyPercentage: Float
                    val result = percentageCalculator(newDifficulty, oldDifficulty)
                    if (result == 0f)
                        difficultyPercentage = 0f
                    else
                        difficultyPercentage = -result
                    when (java.lang.Float.compare(difficultyPercentage, 0f)) {
                        0 -> holder.oldData.text = "0%"
                        1 -> {
                            val positiveText = "+" + decimalFormat
                                    .format(difficultyPercentage.toDouble()) + "%"
                            holder.oldData.text = positiveText
                            holder.oldData.setTextColor(Color.GREEN)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0)
                        }
                        -1 -> {
                            val negativeText = decimalFormat
                                    .format((-difficultyPercentage).toDouble()) + "%"
                            holder.oldData.text = negativeText
                            holder.oldData.setTextColor(Color.RED)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0)
                        }
                    }
                }
                3 -> {
                    val newBlock = java.lang.Float.parseFloat(
                            content.body.replace("[^\\d.]".toRegex(), ""))
                    val oldBlock = java.lang.Float.parseFloat(
                            decimalFormat.format((java.lang.Float.parseFloat(
                                    content.oldData) / 10).toDouble()))
                    val blockPercentage = percentageCalculator(newBlock, oldBlock)
                    when (java.lang.Float.compare(blockPercentage, 0f)) {
                        0 -> holder.oldData.text = "0%"
                        1 -> {
                            val positiveText = "+" + decimalFormat.format(blockPercentage.toDouble()) + "%"
                            holder.oldData.text = positiveText
                            holder.oldData.setTextColor(Color.GREEN)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0)
                        }
                        -1 -> {
                            val negativeText = decimalFormat.format(blockPercentage.toDouble()) + "%"
                            holder.oldData.text = negativeText
                            holder.oldData.setTextColor(Color.RED)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0)
                        }
                    }
                }
                4 -> {
                    val newMinutes = java.lang.Float.parseFloat(
                            content.body.replace("[^\\d.]".toRegex(), ""))
                    val oldMinutes = java.lang.Float.parseFloat(
                            decimalFormat.format(java.lang.Float.parseFloat(content.oldData).toDouble()))
                    val minutesPercentage: Float
                    val resultMinutes = percentageCalculator(newMinutes, oldMinutes)
                    if (resultMinutes == 0f)
                        minutesPercentage = 0f
                    else
                        minutesPercentage = -resultMinutes
                    when (java.lang.Float.compare(minutesPercentage, 0f)) {
                        0 -> holder.oldData.text = "0%"
                        1 -> {
                            val positiveText = "+" + decimalFormat
                                    .format(minutesPercentage.toDouble()) + "%"
                            holder.oldData.text = positiveText
                            holder.oldData.setTextColor(Color.GREEN)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0)
                        }
                        -1 -> {
                            val negativeText = decimalFormat.format(minutesPercentage.toDouble()) + "%"
                            holder.oldData.text = negativeText
                            holder.oldData.setTextColor(Color.RED)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0)
                        }
                    }
                }
                5 -> {
                    val newBtcFees = java.lang.Float.parseFloat(
                            content.body.replace("[^\\d.]".toRegex(), ""))
                    val oldBtcFees = java.lang.Float.parseFloat(
                            decimalFormat.format((java.lang.Float.parseFloat(
                                    content.oldData) / 10000000).toDouble()))
                    val feePercentage: Float
                    val resultFees = percentageCalculator(newBtcFees, oldBtcFees)
                    if (resultFees == 0f)
                        feePercentage = 0f
                    else
                        feePercentage = -resultFees
                    when (java.lang.Float.compare(feePercentage, 0f)) {
                        0 -> holder.oldData.text = "0%"
                        1 -> {
                            val positiveText = "+" + decimalFormat.format(feePercentage.toDouble()) + "%"
                            holder.oldData.text = positiveText
                            holder.oldData.setTextColor(Color.GREEN)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0)
                        }
                        -1 -> {
                            val negativeText = decimalFormat.format(feePercentage.toDouble()) + "%"
                            holder.oldData.text = negativeText
                            holder.oldData.setTextColor(Color.RED)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0)
                        }
                    }
                }
                6 -> {
                    val newTrans = java.lang.Float.parseFloat(
                            content.body.replace("[^\\d.]".toRegex(), ""))
                    val oldTrans = java.lang.Float.parseFloat(
                            decimalFormat.format(java.lang.Float.parseFloat(content.oldData).toDouble()))
                    val transPercentage = percentageCalculator(newTrans, oldTrans)
                    when (java.lang.Float.compare(transPercentage, 0f)) {
                        0 -> holder.oldData.text = "0%"
                        1 -> {
                            val positiveText = "+" + decimalFormat.format(transPercentage.toDouble()) + "%"
                            holder.oldData.text = positiveText
                            holder.oldData.setTextColor(Color.GREEN)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0)
                        }
                        -1 -> {
                            val negativeText = decimalFormat.format(transPercentage.toDouble()) + "%"
                            holder.oldData.text = negativeText
                            holder.oldData.setTextColor(Color.RED)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0)
                        }
                    }
                }
                7 -> {
                    val newBenefit = java.lang.Float.parseFloat(
                            content.body.replace("[^\\d.]".toRegex(), ""))
                    val oldBenefit = java.lang.Float.parseFloat(
                            decimalFormat.format((java.lang.Float.parseFloat(
                                    content.oldData) / 100).toDouble()))
                    val benefitPercentage = percentageCalculator(newBenefit, oldBenefit)
                    when (java.lang.Float.compare(benefitPercentage, 0f)) {
                        0 -> holder.oldData.text = "0%"
                        1 -> {
                            val positiveText = "+" + decimalFormat
                                    .format(benefitPercentage.toDouble()) + "%"
                            holder.oldData.text = positiveText
                            holder.oldData.setTextColor(Color.GREEN)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0)
                        }
                        -1 -> {
                            val negativeText = decimalFormat.format(benefitPercentage.toDouble()) + "%"
                            holder.oldData.text = negativeText
                            holder.oldData.setTextColor(Color.RED)
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0)
                        }
                    }
                }
                else -> holder.oldData.text = content.oldData
            }
            holder.v.setOnLongClickListener {
                when (position) {
                    0 -> MaterialDialog.Builder(context)
                            .title(context.getString(R.string.market_price))
                            .content(R.string.market_price_desc, true)
                            .cancelable(true)
                            .positiveText(R.string.accept)
                            .build().show()
                    1 -> MaterialDialog.Builder(context)
                            .title(context.getString(R.string.hash_rate))
                            .content(R.string.hash_rate_desc, true)
                            .cancelable(true)
                            .positiveText(R.string.accept)
                            .build().show()
                    2 -> MaterialDialog.Builder(context)
                            .title(context.getString(R.string.difficulty))
                            .content(R.string.difficulty_desc, true)
                            .cancelable(true)
                            .positiveText(R.string.accept)
                            .build().show()
                    3 -> MaterialDialog.Builder(context)
                            .title(context.getString(R.string.min_blocks))
                            .content(R.string.min_blocks_desc, true)
                            .cancelable(true)
                            .positiveText(R.string.accept)
                            .build().show()
                    4 -> MaterialDialog.Builder(context)
                            .title(context.getString(R.string.minutes_blocks))
                            .content(R.string.minutes_blocks_desc, true)
                            .cancelable(true)
                            .positiveText(R.string.accept)
                            .build().show()
                    5 -> MaterialDialog.Builder(context)
                            .title(context.getString(R.string.total_fees))
                            .content(R.string.total_fees_desc, true)
                            .cancelable(true)
                            .positiveText(R.string.accept)
                            .build().show()
                    6 -> MaterialDialog.Builder(context)
                            .title(context.getString(R.string.total_trans))
                            .content(R.string.total_trans_desc, true)
                            .cancelable(true)
                            .positiveText(R.string.accept)
                            .build().show()
                    7 -> MaterialDialog.Builder(context)
                            .title(context.getString(R.string.min_benefit))
                            .content(R.string.min_benefit_desc, true)
                            .cancelable(true)
                            .positiveText(R.string.accept)
                            .build().show()
                    else -> {
                    }
                }
                false
            }
        }
    }

    /**
     * Based on: https://www.calculatorsoup.com/calculators/algebra/percent-difference-calculator.php
     *
     * @param newValue new value
     * @param oldValue old value
     *
     * @return difference in percentage of values
     */
    private fun percentageCalculator(newValue: Float, oldValue: Float): Float {
        if (newValue == oldValue)
            return 0f
        else {
            val difference = newValue - oldValue
            val averageValues = (newValue + oldValue) / 2
            val diffDividedAverage = difference / averageValues
            return diffDividedAverage * 100
        }
    }

    override fun getItemCount(): Int {
        return btcData.size
    }

    inner class MyViewHolder internal constructor(internal var v: View) : RecyclerView.ViewHolder(v) {
        internal var title: TextView
        internal var body: TextView
        internal var oldData: TextView

        init {
            title = v.findViewById(R.id.title_text)
            body = v.findViewById(R.id.body_text)
            oldData = v.findViewById(R.id.balance)
        }
    }
}
