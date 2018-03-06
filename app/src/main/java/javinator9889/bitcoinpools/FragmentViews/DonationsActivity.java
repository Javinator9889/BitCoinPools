package javinator9889.bitcoinpools.FragmentViews;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.sufficientlysecure.donations.BuildConfig;
import org.sufficientlysecure.donations.DonationsFragment;

import javinator9889.bitcoinpools.Constants;
import javinator9889.bitcoinpools.R;

import static javinator9889.bitcoinpools.Constants.PAYMENTS.GOOGLE_CATALOG;
import static javinator9889.bitcoinpools.Constants.PAYMENTS.GOOGLE_PUBKEY;

/**
 * Created by Javinator9889 on 01/03/2018.
 * Based on lib: https://github.com/PrivacyApps/donations
 */

public class DonationsActivity extends FragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donations_activity);

        if (isGooglePlayServicesAvaiable(this)) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            DonationsFragment donationsFragment;

            donationsFragment = DonationsFragment.newInstance(BuildConfig.DEBUG, true, GOOGLE_PUBKEY, GOOGLE_CATALOG,
                    getResources().getStringArray(R.array.donation_google_catalog_values), false, null, null,
                    null, false, null, null, false, null);

            fragmentTransaction.replace(R.id.donations_activity_container, donationsFragment, "donationsFragment");
            fragmentTransaction.commit();
        }

        Button paypalButton = findViewById(R.id.donations__paypal_modified_donate_button);
        paypalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(Constants.PAYMENTS.PAYPALME);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("donationsFragment");
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public boolean isGooglePlayServicesAvaiable(Activity activity) {
        GoogleApiAvailability googleApiAvailabilityForPlayServices = GoogleApiAvailability.getInstance();
        int status = googleApiAvailabilityForPlayServices.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailabilityForPlayServices.isUserResolvableError(status)) {
                googleApiAvailabilityForPlayServices.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }
}
