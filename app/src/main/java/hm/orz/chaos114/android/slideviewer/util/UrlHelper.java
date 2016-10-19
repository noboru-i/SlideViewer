package hm.orz.chaos114.android.slideviewer.util;

import android.net.Uri;

import timber.log.Timber;

public final class UrlHelper {
    private UrlHelper() {
        // no-op
    }

    public static boolean isSpeakerDeckUrl(Uri uri) {
        if (!"https".equals(uri.getScheme())) {
            Timber.d("unexpected scheme");
            return false;
        }
        if (!"speakerdeck.com".equals(uri.getHost())) {
            Timber.d("unexpected host");
            return false;
        }
        return true;
    }

    public static boolean canOpen(Uri uri) {
        if (uri.getPathSegments().size() < 2) {
            Timber.d("unexpected path segments");
            return false;
        }
        return true;
    }
}
