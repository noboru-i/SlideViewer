package hm.orz.chaos114.android.slideviewer;

import android.app.Activity;
import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import hm.orz.chaos114.android.slideviewer.di.DaggerAppComponent;
import hm.orz.chaos114.android.slideviewer.ui.CrashReportingTree;
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager;
import timber.log.Timber;

public class SlideViewerApplication extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();

        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this);

        LeakCanary.install(this);
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashReportingTree());

        AnalyticsManager.initializeAnalyticsTracker(this);
        AnalyticsManager.updateUserProperty();
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}
