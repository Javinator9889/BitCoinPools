package javinator9889.bitcoinpools.BackgroundJobs;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.FileProvider;
import javinator9889.bitcoinpools.Constants;

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
public class ShareDataIntent {
    private static final String AUTHORITY = "javinator9889.bitcoinpools.fileprovider";
    private Context mContext;
    private String mChooserTitle;
    private File mImagesCachePath;

    public ShareDataIntent(@NonNull Context context, @StringRes int chooserTitle) {
        mContext = context;
        mChooserTitle = context.getString(chooserTitle);
        mImagesCachePath = new File(context.getCacheDir(), "images");
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean saveImageToCache(@NonNull Bitmap image) {
        mImagesCachePath.mkdirs();
        String filename = mImagesCachePath + "/chart.png";
        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            return true;
        } catch (FileNotFoundException ignored) {
            // This error should not happen as we tried to create dirs - we return "false" either
            return false;
        } catch (IOException e) {
            Log.w(Constants.LOG.NTAG, "Error while writing file to storage", e);
            return false;
        }
    }

    public Uri getUriForSavedImage() {
        File imageFile = new File(mImagesCachePath, "chart.png");
        return FileProvider.getUriForFile(mContext, AUTHORITY, imageFile);
    }

    public PendingIntent shareImageWithText(@Nullable String text, @NonNull Uri contentUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        if (text != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        }
        shareIntent.setType("image/png");
        System.out.println(shareIntent);
        System.out.println(Intent.createChooser(shareIntent, mChooserTitle));
        return PendingIntent.getActivity(mContext,
                0,
                Intent.createChooser(shareIntent, mChooserTitle),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
