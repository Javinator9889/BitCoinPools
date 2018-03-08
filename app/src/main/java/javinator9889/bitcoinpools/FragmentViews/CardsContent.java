package javinator9889.bitcoinpools.FragmentViews;

import android.support.annotation.Nullable;

/**
 * Created by Javinator9889 on 31/01/2018.
 * Simple class containing cards information
 */

public class CardsContent {
    private String title;
    private String body;
    private String oldData;

    public CardsContent(String title, String body, @Nullable String oldData) {
        this.title = title;
        this.body = body;
        this.oldData = oldData;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public String getOldData() {
        return oldData;
    }
}
