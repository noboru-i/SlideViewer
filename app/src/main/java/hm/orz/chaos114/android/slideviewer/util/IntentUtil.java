package hm.orz.chaos114.android.slideviewer.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class IntentUtil {
    private IntentUtil() {
        // no-op
    }

    public static void browse(Context context, String url) {
        browse(context, Uri.parse(url));
    }

    public static void browse(Context context, Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        context.startActivity(intent);
    }
}
