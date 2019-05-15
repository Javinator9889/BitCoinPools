package javinator9889.bitcoinpools

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_META_DATA
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/*
 * Copyright © 2018 - present | BitCoinPools by Javinator9889

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.

 * Created by Javinator9889 on 18/12/2018 - BitCoinPools.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetActivityTitle()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(BitCoinApp.localeManager.setLocale(base))
    }

    private fun resetActivityTitle() {
        try {
            val info = packageManager.getActivityInfo(componentName,
                    GET_META_DATA)
            if (info.labelRes != 0) {
                setTitle(info.labelRes)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Unable to change activity title - maybe it is not set?", e)
        }

    }

    companion object {
        private val TAG = "BaseActivity"
    }
}
