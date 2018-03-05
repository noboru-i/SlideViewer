package hm.orz.chaos114.android.slideviewer.di

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.slideviewer.SlideViewerApplication

@Module
class AppModule {
    @Singleton
    @Provides
    fun provideContext(application: SlideViewerApplication): SlideViewerApplication {
        return application
    }
}
