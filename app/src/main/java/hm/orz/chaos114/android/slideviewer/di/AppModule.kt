package hm.orz.chaos114.android.slideviewer.di

import android.app.Application
import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.slideviewer.SlideViewerApplication
import hm.orz.chaos114.android.slideviewer.util.AdRequestGenerator
import javax.inject.Singleton

@Module
class AppModule {
    @Singleton
    @Provides
    fun provideContext(application: SlideViewerApplication): SlideViewerApplication {
        return application
    }

    @Singleton
    @Provides
    fun provideAdRequestGenerator(app: Application): AdRequestGenerator {
        return AdRequestGenerator(app)
    }
}
