package hm.orz.chaos114.android.slideviewer.util;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import hm.orz.chaos114.android.slideviewer.R;
import timber.log.Timber;

public final class AnalyticsManager {
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
            Timber.d("Screen View recorded: %s", screenName);
        } else {
            Timber.d("Screen View NOT recorded (analytics disabled or not ready).");
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

            Timber.d("Event recorded:");
            Timber.d("\tCategory: %s", category);
            Timber.d("\tAction: %s", action);
            Timber.d("\tLabel: %s", label);
            Timber.d("\tValue: %s", value);
        } else {
            Timber.d("Analytics event ignored (analytics disabled or not ready).");
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
