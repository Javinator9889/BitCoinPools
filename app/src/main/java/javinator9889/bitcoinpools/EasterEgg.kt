package javinator9889.bitcoinpools

import android.content.Context
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView

/**
 * Created by Javinator9889 on 10/03/2018. Easter Egg :D
 */

class EasterEgg : BaseActivity {
    private val positivePhrases: Array<String>
    private var easterEggVideo: VideoView? = null
    private var isEasterEggCompleted = false
    private var actualStepCount = 0

    constructor() : super()

    private constructor(stringArrayResources: Resources) {
        this.positivePhrases = stringArrayResources.getStringArray(R.array.positivePhrases)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.easter_egg)
        easterEggVideo = findViewById(R.id.easteregg_video)
        super.onCreate(savedInstanceState)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        val videoPath = "android.resource://" + packageName + "/" + R.raw.easteregg
        val videoUri = Uri.parse(videoPath)
        val videoMetadata = getVideoMeasurements(videoUri)
        val widthMeasureSpec = videoMetadata[0]
        val heightMeasureSpec = videoMetadata[1]
        val mediaController = MediaController(this)
        mediaController.setAnchorView(easterEggVideo)

        easterEggVideo!!.setVideoURI(videoUri)
        easterEggVideo!!.requestFocus()
        easterEggVideo!!.setMediaController(mediaController)
        easterEggVideo!!.measure(widthMeasureSpec, heightMeasureSpec)
        easterEggVideo!!.start()
        super.onPostCreate(savedInstanceState)
    }

    private fun getVideoMeasurements(videoUri: Uri): IntArray {
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(this, videoUri)
        return intArrayOf(Integer.parseInt(metadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)), Integer.parseInt(metadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)))
    }

    fun addStep(context: Context): Boolean {
        ++actualStepCount
        compareSteps(context)
        return isEasterEggCompleted
    }

    fun resetSteps() {
        actualStepCount = 0
    }

    private fun compareSteps(context: Context) {
        val phraseNumber = (Math.random() * 4).toInt()
        Toast.makeText(context, positivePhrases[phraseNumber], Toast.LENGTH_SHORT).show()
        if (actualStepCount >= 5) {
            isEasterEggCompleted = true
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)
    }

    companion object {

        fun newInstance(applicationResources: Resources): EasterEgg {
            return EasterEgg(applicationResources)
        }
    }
}
