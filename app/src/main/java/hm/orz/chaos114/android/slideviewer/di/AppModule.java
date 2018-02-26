package hm.orz.chaos114.android.slideviewer.di;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hm.orz.chaos114.android.slideviewer.SlideViewerApplication;

@Module
public class AppModule {
    @Singleton
    @Provides
    public SlideViewerApplication provideContext(SlideViewerApplication application) {
        return application;
    }
}
