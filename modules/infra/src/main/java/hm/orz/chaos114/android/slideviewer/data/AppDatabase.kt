package hm.orz.chaos114.android.slideviewer.data

import android.app.Application
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import hm.orz.chaos114.android.slideviewer.data.dao.SlideDao
import hm.orz.chaos114.android.slideviewer.data.dao.TalkDao
import hm.orz.chaos114.android.slideviewer.data.dao.TalkMetaDataDao
import hm.orz.chaos114.android.slideviewer.data.entities.SlideEntity
import hm.orz.chaos114.android.slideviewer.data.entities.TalkEntity
import hm.orz.chaos114.android.slideviewer.data.entities.TalkMetaDataEntity

@Database(entities = [
    (SlideEntity::class),
    (TalkEntity::class),
    (TalkMetaDataEntity::class)
], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun slideDao(): SlideDao
    abstract fun talkDao(): TalkDao
    abstract fun talkMetaDataDao(): TalkMetaDataDao

    companion object {
        fun createInstance(application: Application): AppDatabase =
                Room.databaseBuilder(application, AppDatabase::class.java, "slide_viewer.db")
                        .fallbackToDestructiveMigration()
                        .build()
    }
}
