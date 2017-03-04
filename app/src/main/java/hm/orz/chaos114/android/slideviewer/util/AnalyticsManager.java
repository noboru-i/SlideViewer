package hm.orz.chaos114.android.slideviewer.util;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Locale;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.dao.TalkDao;
import timber.log.Timber;

public final class AnalyticsManager {
    private static Context sAppContext = null;
    private static Tracker mTracker;
    private static FirebaseAnalytics mFirebaseAnalytics;

    private AnalyticsManager() {
        // no-op
    }

    public static void sendScreenView(String screenName) {
        if (!canSend()) {
            Timber.d("Screen View NOT recorded (analytics disabled or not ready).");
            return;
        }

        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.AppViewBuilder().build());
        Timber.d("Screen View recorded: %s", screenName);
    }

    public static void sendChangePageEvent(String category, String url, int page) {
        sendEvent(category, "CHANGE_PAGE", Integer.toString(page));

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getPath(url));
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "slide");
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, "slide");
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, page);
        sendFirebaseEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
    }

    public static void sendStartEvent(String category, String url) {
        sendEvent(category, "START", url);

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
        if (mTracker == null) {
            mTracker = GoogleAnalytics.getInstance(context).newTracker(R.xml.global_tracker);
        }
        if (mFirebaseAnalytics == null) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }
    }

    private static boolean canSend() {
        return sAppContext != null && mTracker != null;
    }

    private static void sendEvent(String category, String action, String label) {
        if (!canSend()) {
            Timber.d("Analytics event ignored (analytics disabled or not ready).");
            return;
        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(0)
                .build());
    }

    private static void sendFirebaseEvent(String event, Bundle bundle) {
        mFirebaseAnalytics.logEvent(event, bundle);
    }

    private static String getPath(String url) {
        Uri uri = Uri.parse(url);
        return uri.getPath();
    }
}
