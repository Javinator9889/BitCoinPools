package javinator9889.bitcoinpools;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * Created by Javinator9889 on 10/03/2018.
 * Easter Egg :D
 */

public class EasterEgg extends Activity {
    private String[] positivePhrases;
    private VideoView easterEggVideo;
    private String[] negativePhrases;
    private boolean isEasterEggCompleted = false;
    private int actualStepCount = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        System.out.println("Creating activity");
        setContentView(R.layout.easter_egg);
        easterEggVideo = findViewById(R.id.easteregg_video);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        System.out.println("Activity created");

        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.easteregg;
        Uri videoUri = Uri.parse(videoPath);
        int videoMetadata[] = getVideoMeasurements(videoUri);
        int widthMeasureSpec = videoMetadata[0];
        int heightMeasureSpec = videoMetadata[1];
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(easterEggVideo);

        easterEggVideo.setVideoURI(videoUri);
        easterEggVideo.requestFocus();
        easterEggVideo.setMediaController(mediaController);
        easterEggVideo.measure(widthMeasureSpec, heightMeasureSpec);
        easterEggVideo.start();
        super.onPostCreate(savedInstanceState);
    }

    @NonNull
    private int[] getVideoMeasurements(Uri videoUri) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(this, videoUri);
        return new int[] {
                Integer.parseInt(metadataRetriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),
                Integer.parseInt(metadataRetriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        };
    }

    public EasterEgg() {
        super();
    }

    private EasterEgg(Resources stringArrayResources) {
        this.positivePhrases = stringArrayResources.getStringArray(R.array.positivePhrases);
        //this.negativePhrases = stringArrayResources.getStringArray(R.array.negativePhrases);
    }

    public static EasterEgg newInstance(Resources applicationResources) {
        return new EasterEgg(applicationResources);
    }

    public boolean addStep(Context context) {
        ++actualStepCount;
        compareSteps(context);
        return isEasterEggCompleted;
    }

    public void resetSteps() {
        actualStepCount = 0;
    }

    private void compareSteps(Context context) {
        int phraseNumber = (int) (Math.random() * 4);
        Toast.makeText(context, positivePhrases[phraseNumber], Toast.LENGTH_SHORT).show();
        if (actualStepCount >= 5) {
            isEasterEggCompleted = true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }
}
