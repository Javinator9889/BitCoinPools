package javinator9889.bitcoinpools;

import android.os.Bundle;
import android.util.Log;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

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
        String[] libs = new String[] {
                getString(R.string.library_donations_libraryName),
                getString(R.string.library_appcompat_v7_libraryName),
                getString(R.string.library_constraint_layout_libraryName),
                getString(R.string.library_design_libraryName),
                getString(R.string.library_recyclerview_v7_libraryName),
                getString(R.string.library_support_annotations_libraryName),
                getString(R.string.library_support_cardview_libraryName),
        };
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
                .withAboutDescription(getString(R.string.bitcoindesc))
                .withLicenseDialog(true)
                .withLibraries(libs)
                .withAutoDetect(true)
                .supportFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        Log.d(Constants.LOG.LTAG, Constants.LOG.INIT_L);
    }
}
