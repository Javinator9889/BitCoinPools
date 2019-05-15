package javinator9889.bitcoinpools

import android.content.Context
import java.io.*
import java.util.*

/**
 * Created by Javinator9889 on 02/03/2018. Created for managing cache and writing data
 */

class CacheManaging private constructor(applicationContext: Context) {
    private val filename: String

    init {
        this.filename = applicationContext.cacheDir.path + File.separator + "DataCache"
    }

    @Throws(IOException::class)
    fun setupFile(): Boolean {
        val cacheFile = File(filename)
        return cacheFile.createNewFile()
    }

    @Throws(IOException::class)
    fun writeCache(objectData: HashMap<String, String>) {
        val cacheDir = File(filename)
        val outputFile = ObjectOutputStream(FileOutputStream(cacheDir))
        outputFile.writeObject(objectData)
        outputFile.flush()
        outputFile.close()
    }

    fun readCache(): HashMap<String, String>? {
        try {
            val cacheDir = File(filename)
            val inputFile = ObjectInputStream(FileInputStream(cacheDir))
            val readData = inputFile.readObject()
            inputFile.close()
            return readData as HashMap<String, String>
        } catch (e: Exception) {
            return null
        }

    }

    companion object {

        fun newInstance(applicationContext: Context): CacheManaging {
            return CacheManaging(applicationContext)
        }
    }
}
