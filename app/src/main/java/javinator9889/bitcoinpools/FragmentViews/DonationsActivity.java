package javinator9889.bitcoinpools.FragmentViews;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
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
    private DonationsFragment donationsFragment = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donations_activity);

        if ((isGooglePlayServicesAvailable(this)) && (Build.VERSION.SDK_INT > 22)) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                    .beginTransaction();

            this.donationsFragment = DonationsFragment.newInstance(BuildConfig.DEBUG, true,
                    GOOGLE_PUBKEY, GOOGLE_CATALOG,
                    getResources().getStringArray(R.array.donation_google_catalog_values),
                    false, null, null,
                    null, false, null,
                    null, false, null);

            fragmentTransaction.replace(R.id.donations_activity_container, this.donationsFragment,
                    "donationsFragment");
            fragmentTransaction.commit();
        }

        Button payPalButton = findViewById(R.id.donations__paypal_modified_donate_button);
        payPalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(Constants.PAYMENTS.PAYPALME);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(browserIntent);
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        try {
            Button googleDonationsButton = donationsFragment.getActivity()
                    .findViewById(R.id.donations__google_android_market_donate_button);
            googleDonationsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        donationsFragment.donateGoogleOnClick(v);
                    } catch (IllegalStateException e) {
                        new MaterialDialog.Builder(DonationsActivity.this)
                                .title(R.string.donations__google_android_market_not_supported_title)
                                .content(R.string.donations__google_android_market_not_supported)
                                .cancelable(true)
                                .positiveText(R.string.accept)
                                .build().show();

                    }
                }
            });
        } catch (NullPointerException e) {
            Log.e("DonationsActivity", "Unable to get button-fragment. Full trace: "
            + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
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

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailabilityForPlayServices = GoogleApiAvailability
                .getInstance();
        int status = googleApiAvailabilityForPlayServices.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailabilityForPlayServices.isUserResolvableError(status)) {
                googleApiAvailabilityForPlayServices.getErrorDialog(activity, status, 2404)
                        .show();
            }
            return false;
        }
        return true;
    }
}
