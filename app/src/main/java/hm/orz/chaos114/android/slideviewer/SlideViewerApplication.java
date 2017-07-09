package hm.orz.chaos114.android.slideviewer;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.squareup.leakcanary.LeakCanary;

import hm.orz.chaos114.android.slideviewer.ui.CrashReportingTree;
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager;
import timber.log.Timber;

public class SlideViewerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        LeakCanary.install(this);
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashReportingTree());

        AnalyticsManager.initializeAnalyticsTracker(this);
        AnalyticsManager.updateUserProperty();

        if (true) {
            Integer i = null;
            i.toString();

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.commit();
        }
    }
}
