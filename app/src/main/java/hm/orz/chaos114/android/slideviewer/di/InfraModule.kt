package hm.orz.chaos114.android.slideviewer.di

import android.app.Application
import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.slideviewer.infra.network.SlideShareLoader
import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository
import javax.inject.Singleton

@Module
class InfraModule {
    @Singleton
    @Provides
    fun provideTalkRepository(app: Application): TalkRepository {
        return TalkRepository(app)
    }

    @Singleton
    @Provides
    fun provideSettingsRepository(app: Application): SettingsRepository {
        return SettingsRepository(app)
    }

    @Singleton
    @Provides
    fun provideSlideShareLoader(talkRepository: TalkRepository): SlideShareLoader {
        return SlideShareLoader(talkRepository)
    }
}
