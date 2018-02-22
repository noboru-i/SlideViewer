package hm.orz.chaos114.android.slideviewer.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;
import hm.orz.chaos114.android.slideviewer.SlideViewerApplication;
import hm.orz.chaos114.android.slideviewer.domain.di.DatabaseModule;

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        DatabaseModule.class,
        ActivityModule.class
})
public interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        Builder databaseModule(DatabaseModule databaseModule);

        AppComponent build();
    }

    void inject(SlideViewerApplication app);
}