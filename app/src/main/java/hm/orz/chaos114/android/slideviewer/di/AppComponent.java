package hm.orz.chaos114.android.slideviewer.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import hm.orz.chaos114.android.slideviewer.SlideViewerApplication;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        OcrModule.class,
        AppModule.class,
        ActivityModule.class
})
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    void inject(SlideViewerApplication app);
}
