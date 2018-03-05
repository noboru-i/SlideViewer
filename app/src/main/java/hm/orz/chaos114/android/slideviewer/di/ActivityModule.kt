package hm.orz.chaos114.android.slideviewer.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import hm.orz.chaos114.android.slideviewer.ui.SlideActivity
import hm.orz.chaos114.android.slideviewer.ui.SlideListActivity

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeSlideActivityInjector(): SlideActivity

    @ContributesAndroidInjector
    abstract fun contributeSlideListActivityInjector(): SlideListActivity
}
