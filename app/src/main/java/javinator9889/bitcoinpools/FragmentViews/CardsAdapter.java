package javinator9889.bitcoinpools.FragmentViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import javinator9889.bitcoinpools.R;

/**
 * Created by Javinator9889 on 31/01/2018. Based on: https://www.androidhive.info/2016/05/android-working-with-card-view-and-recycler-view/
 */

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.MyViewHolder> {
    private Context context;
    private List<CardsContent> btcData;

    CardsAdapter(Context context, List<CardsContent> btcData) {
        this.context = context;
        this.btcData = btcData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bitcoin_card,
                parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder,
                                 @SuppressLint("RecyclerView") final int position) {
        final CardsContent content = btcData.get(position);
        holder.title.setText(content.getTitle());
        holder.body.setText(content.getBody());
        DecimalFormat decimalFormat = new DecimalFormat("#.##",
                new DecimalFormatSymbols(Locale.US));
        if (content.getOldData() == null)
            holder.oldData.setText("");
        else {
            switch (position) {
                case 0:
                    float newPrice = Float.parseFloat(
                            content.getBody().replaceAll("[^\\d.]", ""));
                    float oldPrice = Float.parseFloat(
                            decimalFormat.format(Float.parseFloat(content.getOldData())));
                    float pricePercentage = percentageCalculator(newPrice, oldPrice);
                    switch (Float.compare(pricePercentage, 0f)) {
                        case 0:
                            holder.oldData.setText("0%");
                            break;
                        case 1:
                            String positiveText = "+" + decimalFormat.format(pricePercentage) + "%";
                            holder.oldData.setText(positiveText);
                            holder.oldData.setTextColor(Color.GREEN);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0);
                            break;
                        case -1:
                            String negativeText = decimalFormat.format(pricePercentage) + "%";
                            holder.oldData.setText(negativeText);
                            holder.oldData.setTextColor(Color.RED);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0);
                            break;
                    }
                    break;
                case 1:
                    float newPower = Float.parseFloat(
                            content.getBody().replaceAll("[^\\d.]", ""));
                    float oldPower = Float.parseFloat(
                            decimalFormat.format(Float.parseFloat(content.getOldData())));
                    float powerPercentage = percentageCalculator(newPower, oldPower);
                    switch (Float.compare(powerPercentage, 0f)) {
                        case 0:
                            holder.oldData.setText("0%");
                            break;
                        case 1:
                            String positiveText = "+" + decimalFormat.format(powerPercentage) + "%";
                            holder.oldData.setText(positiveText);
                            holder.oldData.setTextColor(Color.GREEN);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0);
                            break;
                        case -1:
                            String negativeText = decimalFormat.format(powerPercentage) + "%";
                            holder.oldData.setText(negativeText);
                            holder.oldData.setTextColor(Color.RED);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0);
                            break;
                    }
                    break;
                case 2:
                    float newDifficulty = Float.parseFloat(
                            content.getBody().replaceAll("[^\\d.]", ""));
                    float oldDifficulty = Float.parseFloat(
                            decimalFormat.format(Float.parseFloat(content.getOldData())));
                    float difficultyPercentage;
                    float result = percentageCalculator(newDifficulty, oldDifficulty);
                    if (result == 0)
                        difficultyPercentage = 0;
                    else
                        difficultyPercentage = -result;
                    switch (Float.compare(difficultyPercentage, 0f)) {
                        case 0:
                            holder.oldData.setText("0%");
                            break;
                        case 1:
                            String positiveText = "+" + decimalFormat
                                    .format(difficultyPercentage) + "%";
                            holder.oldData.setText(positiveText);
                            holder.oldData.setTextColor(Color.GREEN);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0);
                            break;
                        case -1:
                            String negativeText = decimalFormat
                                    .format(-difficultyPercentage) + "%";
                            holder.oldData.setText(negativeText);
                            holder.oldData.setTextColor(Color.RED);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0);
                            break;
                    }
                    break;
                case 3:
                    float newBlock = Float.parseFloat(
                            content.getBody().replaceAll("[^\\d.]", ""));
                    float oldBlock = Float.parseFloat(
                            decimalFormat.format(Float.parseFloat(
                                    content.getOldData()) / 10));
                    float blockPercentage = percentageCalculator(newBlock, oldBlock);
                    switch (Float.compare(blockPercentage, 0f)) {
                        case 0:
                            holder.oldData.setText("0%");
                            break;
                        case 1:
                            String positiveText = "+" + decimalFormat.format(blockPercentage) + "%";
                            holder.oldData.setText(positiveText);
                            holder.oldData.setTextColor(Color.GREEN);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0);
                            break;
                        case -1:
                            String negativeText = decimalFormat.format(blockPercentage) + "%";
                            holder.oldData.setText(negativeText);
                            holder.oldData.setTextColor(Color.RED);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0);
                            break;
                    }
                    break;
                case 4:
                    float newMinutes = Float.parseFloat(
                            content.getBody().replaceAll("[^\\d.]", ""));
                    float oldMinutes = Float.parseFloat(
                            decimalFormat.format(Float.parseFloat(content.getOldData())));
                    float minutesPercentage;
                    float resultMinutes = percentageCalculator(newMinutes, oldMinutes);
                    if (resultMinutes == 0)
                        minutesPercentage = 0;
                    else
                        minutesPercentage = -resultMinutes;
                    switch (Float.compare(minutesPercentage, 0f)) {
                        case 0:
                            holder.oldData.setText("0%");
                            break;
                        case 1:
                            String positiveText = "+" + decimalFormat
                                    .format(minutesPercentage) + "%";
                            holder.oldData.setText(positiveText);
                            holder.oldData.setTextColor(Color.GREEN);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0);
                            break;
                        case -1:
                            String negativeText = decimalFormat.format(minutesPercentage) + "%";
                            holder.oldData.setText(negativeText);
                            holder.oldData.setTextColor(Color.RED);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0);
                            break;
                    }
                    break;
                case 5:
                    float newBtcFees = Float.parseFloat(
                            content.getBody().replaceAll("[^\\d.]", ""));
                    float oldBtcFees = Float.parseFloat(
                            decimalFormat.format(Float.parseFloat(
                                    content.getOldData()) / 10000000));
                    float feePercentage;
                    float resultFees = percentageCalculator(newBtcFees, oldBtcFees);
                    if (resultFees == 0)
                        feePercentage = 0;
                    else
                        feePercentage = -resultFees;
                    switch (Float.compare(feePercentage, 0f)) {
                        case 0:
                            holder.oldData.setText("0%");
                            break;
                        case 1:
                            String positiveText = "+" + decimalFormat.format(feePercentage) + "%";
                            holder.oldData.setText(positiveText);
                            holder.oldData.setTextColor(Color.GREEN);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0);
                            break;
                        case -1:
                            String negativeText = decimalFormat.format(feePercentage) + "%";
                            holder.oldData.setText(negativeText);
                            holder.oldData.setTextColor(Color.RED);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0);
                            break;
                    }
                    break;
                case 6:
                    float newTrans = Float.parseFloat(
                            content.getBody().replaceAll("[^\\d.]", ""));
                    float oldTrans = Float.parseFloat(
                            decimalFormat.format(Float.parseFloat(content.getOldData())));
                    float transPercentage = percentageCalculator(newTrans, oldTrans);
                    switch (Float.compare(transPercentage, 0f)) {
                        case 0:
                            holder.oldData.setText("0%");
                            break;
                        case 1:
                            String positiveText = "+" + decimalFormat.format(transPercentage) + "%";
                            holder.oldData.setText(positiveText);
                            holder.oldData.setTextColor(Color.GREEN);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0);
                            break;
                        case -1:
                            String negativeText = decimalFormat.format(transPercentage) + "%";
                            holder.oldData.setText(negativeText);
                            holder.oldData.setTextColor(Color.RED);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0);
                            break;
                    }
                    break;
                case 7:
                    float newBenefit = Float.parseFloat(
                            content.getBody().replaceAll("[^\\d.]", ""));
                    float oldBenefit = Float.parseFloat(
                            decimalFormat.format(Float.parseFloat(
                                    content.getOldData()) / 100));
                    float benefitPercentage = percentageCalculator(newBenefit, oldBenefit);
                    switch (Float.compare(benefitPercentage, 0f)) {
                        case 0:
                            holder.oldData.setText("0%");
                            break;
                        case 1:
                            String positiveText = "+" + decimalFormat
                                    .format(benefitPercentage) + "%";
                            holder.oldData.setText(positiveText);
                            holder.oldData.setTextColor(Color.GREEN);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_green_arrow_up_darker, 0, 0, 0);
                            break;
                        case -1:
                            String negativeText = decimalFormat.format(benefitPercentage) + "%";
                            holder.oldData.setText(negativeText);
                            holder.oldData.setTextColor(Color.RED);
                            holder.oldData.setCompoundDrawablesWithIntrinsicBounds(
                                    R.drawable.ic_red_arrow_down, 0, 0, 0);
                            break;
                    }
                    break;
                default:
                    holder.oldData.setText(content.getOldData());
            }
            holder.v.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    switch (position) {
                        case 0:
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.market_price))
                                    .content(R.string.market_price_desc, true)
                                    .cancelable(true)
                                    .positiveText(R.string.accept)
                                    .build().show();
                            break;
                        case 1:
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.hash_rate))
                                    .content(R.string.hash_rate_desc, true)
                                    .cancelable(true)
                                    .positiveText(R.string.accept)
                                    .build().show();
                            break;
                        case 2:
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.difficulty))
                                    .content(R.string.difficulty_desc, true)
                                    .cancelable(true)
                                    .positiveText(R.string.accept)
                                    .build().show();
                            break;
                        case 3:
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.min_blocks))
                                    .content(R.string.min_blocks_desc, true)
                                    .cancelable(true)
                                    .positiveText(R.string.accept)
                                    .build().show();
                            break;
                        case 4:
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.minutes_blocks))
                                    .content(R.string.minutes_blocks_desc, true)
                                    .cancelable(true)
                                    .positiveText(R.string.accept)
                                    .build().show();
                            break;
                        case 5:
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.total_fees))
                                    .content(R.string.total_fees_desc, true)
                                    .cancelable(true)
                                    .positiveText(R.string.accept)
                                    .build().show();
                            break;
                        case 6:
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.total_trans))
                                    .content(R.string.total_trans_desc, true)
                                    .cancelable(true)
                                    .positiveText(R.string.accept)
                                    .build().show();
                            break;
                        case 7:
                            new MaterialDialog.Builder(context)
                                    .title(context.getString(R.string.min_benefit))
                                    .content(R.string.min_benefit_desc, true)
                                    .cancelable(true)
                                    .positiveText(R.string.accept)
                                    .build().show();
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });
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
    private float percentageCalculator(float newValue, float oldValue) {
        if (newValue == oldValue)
            return 0;
        else {
            float difference = newValue - oldValue;
            float averageValues = (newValue + oldValue) / 2;
            float diffDividedAverage = difference / averageValues;
            return (diffDividedAverage * 100);
        }
    }

    @Override
    public int getItemCount() {
        return btcData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, body, oldData;
        View v;

        MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title_text);
            body = view.findViewById(R.id.body_text);
            oldData = view.findViewById(R.id.balance);
            this.v = view;
        }
    }
}
