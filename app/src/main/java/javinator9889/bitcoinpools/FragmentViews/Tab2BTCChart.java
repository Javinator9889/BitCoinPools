package javinator9889.bitcoinpools.FragmentViews;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javinator9889.bitcoinpools.R;

/**
 * Created by Javinator9889 on 28/01/2018.
 */

public class Tab2BTCChart extends Fragment {
    public Tab2BTCChart() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab2_btcchart, container, false);
    }
}
