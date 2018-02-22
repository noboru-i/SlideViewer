package hm.orz.chaos114.android.slideviewer.domain.di

import android.app.Application
import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.slideviewer.data.AppDatabase
import hm.orz.chaos114.android.slideviewer.data.dao.SlideDao
import hm.orz.chaos114.android.slideviewer.data.dao.TalkDao
import hm.orz.chaos114.android.slideviewer.data.dao.TalkMetaDataDao
import javax.inject.Singleton

@Module
open class DatabaseModule {

    companion object {
        @JvmField
        val instance = DatabaseModule()
    }

    @Singleton
    @Provides
    fun provideAppDatabase(app: Application): AppDatabase = AppDatabase.createInstance(app)

    @Singleton
    @Provides
    fun provideSlideDao(db: AppDatabase): SlideDao = db.slideDao()

    @Singleton
    @Provides
    fun provideTalkDao(db: AppDatabase): TalkDao = db.talkDao()

    @Singleton
    @Provides
    fun provideTalkMetaDataDao(db: AppDatabase): TalkMetaDataDao = db.talkMetaDataDao()
}