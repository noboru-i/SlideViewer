package hm.orz.chaos114.android.slideviewer

import android.app.Activity
import com.google.android.play.core.splitcompat.SplitCompatApplication
import com.squareup.leakcanary.LeakCanary
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import hm.orz.chaos114.android.slideviewer.di.DaggerAppComponent
import hm.orz.chaos114.android.slideviewer.ui.CrashReportingTree
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager
import timber.log.Timber
import javax.inject.Inject

class SlideViewerApplication : SplitCompatApplication(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate() {
        super.onCreate()

        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this)

        LeakCanary.install(this)
        Timber.plant(if (BuildConfig.DEBUG) Timber.DebugTree() else CrashReportingTree())

        analyticsManager.updateUserProperty()
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }
}
