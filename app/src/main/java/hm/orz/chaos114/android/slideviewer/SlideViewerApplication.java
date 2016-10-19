package hm.orz.chaos114.android.slideviewer;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;

public class SlideViewerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        LeakCanary.install(this);
        Timber.plant(new Timber.DebugTree());
    }
}
