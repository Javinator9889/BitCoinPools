package javinator9889.bitcoinpools

import android.os.Bundle
import android.util.Log
import com.mikepenz.aboutlibraries.LibsBuilder

/**
 * Created by Javinator9889 on 22/12/2017. License of the app
 */

class License : BaseActivity() {
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.license)
        setTitle(R.string.licenseTitle)
        val libs = arrayOf(getString(R.string.library_donations_libraryName), getString(R.string.library_appcompat_v7_libraryName), getString(R.string.library_constraint_layout_libraryName), getString(R.string.library_design_libraryName), getString(R.string.library_recyclerview_v7_libraryName), getString(R.string.library_support_annotations_libraryName), getString(R.string.library_support_cardview_libraryName))
        val fragment = LibsBuilder()
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
                .withLibraries(*libs)
                .withAutoDetect(true)
                .supportFragment()

        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit()
        Log.d(Constants.LOG.LTAG, Constants.LOG.INIT_L)
    }
}
