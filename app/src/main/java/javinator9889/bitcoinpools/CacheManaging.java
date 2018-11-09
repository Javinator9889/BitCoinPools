package javinator9889.bitcoinpools;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import androidx.annotation.NonNull;

/**
 * Created by Javinator9889 on 02/03/2018.
 * Created for managing cache and writing data
 */

public class CacheManaging {
    private String filename;

    private CacheManaging(Context applicationContext) {
        this.filename = applicationContext.getCacheDir().getPath() + File.separator + "DataCache";
    }

    @NonNull
    public static CacheManaging newInstance(Context applicationContext) {
        return new CacheManaging(applicationContext);
    }

    public boolean setupFile() throws IOException {
        File cacheFile = new File(filename);
        return cacheFile.createNewFile();
    }

    public void writeCache(HashMap<String, String> objectData) throws IOException {
        File cacheDir = new File(filename);
        ObjectOutputStream outputFile = new ObjectOutputStream(new FileOutputStream(cacheDir));
        outputFile.writeObject(objectData);
        outputFile.flush();
        outputFile.close();
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, String> readCache() {
        try {
            File cacheDir = new File(filename);
            ObjectInputStream inputFile = new ObjectInputStream(new FileInputStream(cacheDir));
            Object readData = inputFile.readObject();
            inputFile.close();
            return (HashMap<String, String>) readData;
        } catch (Exception e) {
            return null;
        }
    }
}
