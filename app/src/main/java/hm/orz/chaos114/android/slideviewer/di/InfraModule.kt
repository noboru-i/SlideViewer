package hm.orz.chaos114.android.slideviewer.di

import android.app.Application

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.slideviewer.infra.network.SlideShareLoader
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository

@Module
class InfraModule {
    @Singleton
    @Provides
    fun provideTalkRepository(app: Application): TalkRepository {
        return TalkRepository(app)
    }

    @Singleton
    @Provides
    fun provideSlideShareLoader(talkRepository: TalkRepository): SlideShareLoader {
        return SlideShareLoader(talkRepository)
    }
}
