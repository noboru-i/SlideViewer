package hm.orz.chaos114.android.slideviewer.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import hm.orz.chaos114.android.slideviewer.BuildConfig;
import hm.orz.chaos114.android.slideviewer.R;

public final class AnalyticsManager {
    private final static String TAG = AnalyticsManager.class.getSimpleName();

    public enum Action {
        START,
        CHANGE_PAGE,
    }

    private static Context sAppContext = null;
    private static Tracker mTracker;

    private AnalyticsManager() {
        // no-op
    }

    private static boolean canSend() {
        return sAppContext != null && mTracker != null;
    }

    public static void sendScreenView(String screenName) {
        if (canSend()) {
            mTracker.setScreenName(screenName);
            mTracker.send(new HitBuilders.AppViewBuilder().build());
            Log.d(TAG, "Screen View recorded: " + screenName);
        } else {
            Log.d(TAG, "Screen View NOT recorded (analytics disabled or not ready).");
        }
    }

    public static void sendEvent(String category, String action, String label, long value) {
        if (canSend()) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .setLabel(label)
                    .setValue(value)
                    .build());

            Log.d(TAG, "Event recorded:");
            Log.d(TAG, "\tCategory: " + category);
            Log.d(TAG, "\tAction: " + action);
            Log.d(TAG, "\tLabel: " + label);
            Log.d(TAG, "\tValue: " + value);
        } else {
            Log.d(TAG, "Analytics event ignored (analytics disabled or not ready).");
        }
    }

    public static void sendEvent(String category, String action, String label) {
        sendEvent(category, action, label, 0);
    }

    public static synchronized void initializeAnalyticsTracker(Context context) {
        sAppContext = context;
        if (mTracker == null) {
            mTracker = GoogleAnalytics.getInstance(context).newTracker(R.xml.global_tracker);
        }
    }
}
