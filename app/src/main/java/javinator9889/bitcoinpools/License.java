package javinator9889.bitcoinpools;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Javinator9889 on 22/12/2017.
 * License of the app
 */

public class License extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.license);
        setTitle(R.string.licenseTitle);
        Log.d(Constants.LOG.LTAG, Constants.LOG.INIT_L);
    }
}
