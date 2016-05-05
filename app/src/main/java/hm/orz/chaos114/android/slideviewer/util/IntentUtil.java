package hm.orz.chaos114.android.slideviewer.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class IntentUtil {
    private IntentUtil() {
        // no-op
    }

    public static void browse(Context context, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }
}
