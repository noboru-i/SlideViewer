package hm.orz.chaos114.android.slideviewer

import android.app.Activity
import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex

import com.squareup.leakcanary.LeakCanary

import javax.inject.Inject

import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import hm.orz.chaos114.android.slideviewer.di.DaggerAppComponent
import hm.orz.chaos114.android.slideviewer.ui.CrashReportingTree
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager
import timber.log.Timber

class SlideViewerApplication : Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this)

        LeakCanary.install(this)
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else CrashReportingTree())

        AnalyticsManager.initializeAnalyticsTracker(this)
        AnalyticsManager.updateUserProperty()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }
}
