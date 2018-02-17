package hm.orz.chaos114.android.slideviewer.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import hm.orz.chaos114.android.slideviewer.data.dao.TalkDao
import hm.orz.chaos114.android.slideviewer.data.entities.SlideEntity
import hm.orz.chaos114.android.slideviewer.data.entities.TalkEntity
import hm.orz.chaos114.android.slideviewer.data.entities.TalkMetaData

@Database(entities = [
    (SlideEntity::class),
    (TalkEntity::class),
    (TalkMetaData::class)
], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun talkDao(): TalkDao
}
