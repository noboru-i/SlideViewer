package hm.orz.chaos114.android.slideviewer;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import hm.orz.chaos114.android.slideviewer.ui.CrashReportingTree;
import timber.log.Timber;

public class SlideViewerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        LeakCanary.install(this);
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashReportingTree());
    }
}
