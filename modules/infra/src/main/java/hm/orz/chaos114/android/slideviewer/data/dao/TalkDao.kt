package hm.orz.chaos114.android.slideviewer.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import android.support.annotation.CheckResult
import hm.orz.chaos114.android.slideviewer.data.entities.TalkEntity
import hm.orz.chaos114.android.slideviewer.data.entities.TalkWithChildrenEntity
import io.reactivex.Flowable

@Dao abstract class TalkDao {
    @CheckResult
    @Transaction
    @Query("SELECT * FROM talk")
    abstract fun fetch(): Flowable<List<TalkWithChildrenEntity>>

    @CheckResult
    @Query("SELECT * FROM talk WHERE url = :url")
    abstract fun findByUrl(url: String): TalkEntity

    @Query("DELETE FROM talk WHERE url = :url")
    abstract fun deleteByUrl(url: String)
}
