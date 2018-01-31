package javinator9889.bitcoinpools.FragmentViews;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import javinator9889.bitcoinpools.R;

/**
 * Created by Javinator9889 on 31/01/2018.
 */

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.MyViewHolder> {
    private Context context;
    private List<CardsContent> btcData;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, body;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title_text);
            body = (TextView) view.findViewById(R.id.body_text);
        }
    }

    public CardsAdapter(Context context, List<CardsContent> btcData) {
        this.context = context;
        this.btcData = btcData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bitcoin_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        CardsContent content = btcData.get(position);
        holder.title.setText(content.getTitle());
        holder.body.setText(content.getBody());
    }

    @Override
    public int getItemCount() {
        return btcData.size();
    }
}
