package hm.orz.chaos114.android.slideviewer.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hm.orz.chaos114.android.slideviewer.infra.network.SlideShareLoader;
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository;

@Module
public class InfraModule {
    @Singleton
    @Provides
    public TalkRepository provideTalkRepository(Application app) {
        return new TalkRepository(app);
    }

    @Singleton
    @Provides
    public SlideShareLoader provideSlideShareLoader(TalkRepository talkRepository) {
        return new SlideShareLoader(talkRepository);
    }
}
