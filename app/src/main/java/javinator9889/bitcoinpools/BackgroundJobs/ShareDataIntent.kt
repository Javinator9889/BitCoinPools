package javinator9889.bitcoinpools.BackgroundJobs

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import javinator9889.bitcoinpools.Constants
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

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

 * Created by Javinator9889 on 16/12/2018 - BitCoinPools.
 */
class ShareDataIntent(private val mContext: Context, @StringRes chooserTitle: Int) {
    private val mChooserTitle: String
    private val mImagesCachePath: File

    val uriForSavedImage: Uri
        get() {
            val imageFile = File(mImagesCachePath, "chart.png")
            return FileProvider.getUriForFile(mContext, AUTHORITY, imageFile)
        }

    init {
        mChooserTitle = mContext.getString(chooserTitle)
        mImagesCachePath = File(mContext.cacheDir, "images")
    }

    fun saveImageToCache(image: Bitmap): Boolean {
        mImagesCachePath.mkdirs()
        val filename = "$mImagesCachePath/chart.png"
        try {
            FileOutputStream(filename).use { outputStream ->
                image.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                return true
            }
        } catch (ignored: FileNotFoundException) {
            // This error should not happen as we tried to create dirs - we return "false" either
            return false
        } catch (e: IOException) {
            Log.w(Constants.LOG.NTAG, "Error while writing file to storage", e)
            return false
        }

    }

    fun shareImageWithText(text: String?, contentUri: Uri): PendingIntent {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
        if (text != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, text)
        }
        shareIntent.type = "image/png"
        println(shareIntent)
        println(Intent.createChooser(shareIntent, mChooserTitle))
        return PendingIntent.getActivity(mContext,
                0,
                Intent.createChooser(shareIntent, mChooserTitle),
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        private val AUTHORITY = "javinator9889.bitcoinpools.fileprovider"
    }
}
