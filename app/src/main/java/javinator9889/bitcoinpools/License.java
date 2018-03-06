package javinator9889.bitcoinpools;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.LibTaskCallback;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.entity.Library;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

/**
 * Created by Javinator9889 on 22/12/2017.
 * License of the app
 */

public class License extends AppCompatActivity {
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.license);
        setTitle(R.string.licenseTitle);
        LibsSupportFragment fragment = new LibsBuilder()
                .withVersionShown(true)
                .withLicenseShown(true)
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAboutAppName("BitCoin Pools")
                .withAboutSpecial1("EULA")
                .withAboutSpecial1Description(getString(R.string.ftos))
                .withAboutSpecial2("Changelog")
                .withAboutSpecial2Description(getString(R.string.changelog))
                .withAutoDetect(true)
                .withAboutDescription(getString(R.string.bitcoindesc))
                .withLicenseDialog(true)
                .withLibraries(getString(R.string.library_donations_libraryName))
                /*.withLibraryModification("Donations", Libs.LibraryFields.LIBRARY_NAME, "Donations")
                .withLibraryModification("Donations", Libs.LibraryFields.LIBRARY_DESCRIPTION,
                        "Android Donations Lib supports donations by Google Play Store, Flattr, PayPal, and Bitcoin.")
                .withLibraryModification("Donations", Libs.LibraryFields.LICENSE_NAME, getString(R.string.license_Apache_2_0_licenseName))
                .withLibraryModification("Donations", Libs.LibraryFields.AUTHOR_NAME, "PrivacyApps")
                .withLibraryModification("Donations", Libs.LibraryFields.LIBRARY_REPOSITORY_LINK, "https://github.com/PrivacyApps/donations")
                .withLibraryModification("Donations", Libs.LibraryFields.LIBRARY_VERSION, "2.5")*/
                .supportFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        Log.d(Constants.LOG.LTAG, Constants.LOG.INIT_L);

    }

    LibTaskCallback libTaskCallback = new LibTaskCallback() {
        @Override
        public void onLibTaskStarted() {
            Log.e("AboutLibraries", "started");
        }

        @Override
        public void onLibTaskFinished(ItemAdapter fastItemAdapter) {
            Log.e("AboutLibraries", "finished");
        }
    };

    LibsConfiguration.LibsUIListener libsUIListener = new LibsConfiguration.LibsUIListener() {
        @Override
        public View preOnCreateView(View view) {
            return view;
        }

        @Override
        public View postOnCreateView(View view) {
            return view;
        }
    };

    LibsConfiguration.LibsListener libsListener = new LibsConfiguration.LibsListener() {
        @Override
        public void onIconClicked(View v) {
            Toast.makeText(v.getContext(), "We are able to track this now ;)", Toast.LENGTH_LONG).show();
        }

        @Override
        public boolean onLibraryAuthorClicked(View v, Library library) {
            return false;
        }

        @Override
        public boolean onLibraryContentClicked(View v, Library library) {
            return false;
        }

        @Override
        public boolean onLibraryBottomClicked(View v, Library library) {
            return false;
        }

        @Override
        public boolean onExtraClicked(View v, Libs.SpecialButton specialButton) {
            return false;
        }

        @Override
        public boolean onIconLongClicked(View v) {
            return false;
        }

        @Override
        public boolean onLibraryAuthorLongClicked(View v, Library library) {
            return false;
        }

        @Override
        public boolean onLibraryContentLongClicked(View v, Library library) {
            return false;
        }

        @Override
        public boolean onLibraryBottomLongClicked(View v, Library library) {
            return false;
        }
    };
}
