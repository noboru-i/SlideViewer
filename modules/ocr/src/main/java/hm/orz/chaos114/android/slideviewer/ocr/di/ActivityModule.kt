package hm.orz.chaos114.android.slideviewer.ocr.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import hm.orz.chaos114.android.slideviewer.ocr.ui.SelectOcrLanguageActivity

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeSelectOcrLanguageActivityInjector(): SelectOcrLanguageActivity
}
