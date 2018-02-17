package hm.orz.chaos114.android.slideviewer.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.support.annotation.CheckResult
import hm.orz.chaos114.android.slideviewer.data.entities.TalkEntity

@Dao abstract class TalkDao {
    @CheckResult
    @Query("SELECT * FROM talk WHERE url = :url")
    abstract fun findByUrl(url: String): TalkEntity

    @Query("DELETE FROM talk WHERE url = :url")
    abstract fun deleteByUrl(url: String)
}
