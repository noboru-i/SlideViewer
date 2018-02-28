package hm.orz.chaos114.android.slideviewer.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository;

@Module
public class DatabaseModule {
    @Singleton
    @Provides
    public TalkRepository provideTalkRepository(Application app) {
        return new TalkRepository(app);
    }
}
