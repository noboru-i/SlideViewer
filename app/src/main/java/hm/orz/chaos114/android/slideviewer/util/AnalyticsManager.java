package hm.orz.chaos114.android.slideviewer.util;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

import hm.orz.chaos114.android.slideviewer.dao.TalkDao;
import timber.log.Timber;

public final class AnalyticsManager {
    private static Context sAppContext = null;
    private static FirebaseAnalytics mFirebaseAnalytics;

    private AnalyticsManager() {
        // no-op
    }

    public static void sendChangePageEvent(String url, int page) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getPath(url));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "slide");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "slide");
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, page);
        sendFirebaseEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    public static void sendStartEvent(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "slide");
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getPath(url));
        sendFirebaseEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public static void updateUserProperty() {
        if (!canSend()) {
            Timber.d("Analytics update user property ignored (analytics disabled or not ready).");
            return;
        }

        TalkDao dao = new TalkDao(sAppContext);
        int count = dao.list().size();
        mFirebaseAnalytics.setUserProperty("slide_count", String.format(Locale.getDefault(), "%d", count));
    }


    public static synchronized void initializeAnalyticsTracker(Context context) {
        sAppContext = context;
        if (mFirebaseAnalytics == null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
    }

    private static boolean canSend() {
        return sAppContext != null;
    }

    private static void sendFirebaseEvent(String event, Bundle bundle) {
        mFirebaseAnalytics.logEvent(event, bundle);
    }

    private static String getPath(String url) {
        Uri uri = Uri.parse(url);
        return uri.getPath();
    }
}
