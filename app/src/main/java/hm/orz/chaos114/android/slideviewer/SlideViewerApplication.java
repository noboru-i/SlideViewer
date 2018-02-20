package hm.orz.chaos114.android.slideviewer;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

import hm.orz.chaos114.android.slideviewer.di.AppComponent;
import hm.orz.chaos114.android.slideviewer.domain.di.DatabaseModule;
import hm.orz.chaos114.android.slideviewer.ui.CrashReportingTree;
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager;
import timber.log.Timber;

public class SlideViewerApplication extends Application {

    private AppComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeInjector();

        LeakCanary.install(this);
        Timber.plant(BuildConfig.DEBUG ? new Timber.DebugTree() : new CrashReportingTree());
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        AnalyticsManager.initializeAnalyticsTracker(this);
        AnalyticsManager.updateUserProperty();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initializeInjector() {
        applicationComponent = DaggerAppComponent.builder()
                .appModule(new DatabaseModule(this))
                .build();
    }

    public AppComponent getApplicationComponent() {
        return applicationComponent;
    }
}
