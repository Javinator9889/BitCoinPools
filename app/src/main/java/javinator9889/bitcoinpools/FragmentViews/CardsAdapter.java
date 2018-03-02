package javinator9889.bitcoinpools.FragmentViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import javinator9889.bitcoinpools.R;

/**
 * Created by Javinator9889 on 31/01/2018.
 * Based on: https://www.androidhive.info/2016/05/android-working-with-card-view-and-recycler-view/
 */

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.MyViewHolder> {
    private Context context;
    private List<CardsContent> btcData;

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

    CardsAdapter(Context context, List<CardsContent> btcData) {
        this.context = context;
        this.btcData = btcData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bitcoin_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final CardsContent content = btcData.get(position);
        holder.title.setText(content.getTitle());
        holder.body.setText(content.getBody());
        if (content.getOldData() == null)
            holder.oldData.setText("");
        else {
            //holder.oldData.setText(content.getOldData());
            holder.oldData.setText(content.getOldData());
            /*switch (position) {
                case 0:
                    holder.oldData.setText(String.valueOf(Float.parseFloat(content.getOldData()) != Float.parseFloat(content.getBody())));
            }*/
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

    @Override
    public int getItemCount() {
        return btcData.size();
    }
}
