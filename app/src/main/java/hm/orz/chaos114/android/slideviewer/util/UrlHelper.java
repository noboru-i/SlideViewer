package hm.orz.chaos114.android.slideviewer.util;

import android.net.Uri;
import android.util.Log;

public final class UrlHelper {
    private static final String TAG = UrlHelper.class.getSimpleName();

    private UrlHelper() {
        // no-op
    }

    public static boolean isSpeakerDeckUrl(Uri uri) {
        if (!"https".equals(uri.getScheme())) {
            Log.d(TAG, "unexpected scheme");
            return false;
        }
        if (!"speakerdeck.com".equals(uri.getHost())) {
            Log.d(TAG, "unexpected host");
            return false;
        }
        return true;
    }

    public static boolean canOpen(Uri uri) {
        if (uri.getPathSegments().size() < 2) {
            Log.d(TAG, "unexpected path segments");
            return false;
        }
        return true;
    }
}
