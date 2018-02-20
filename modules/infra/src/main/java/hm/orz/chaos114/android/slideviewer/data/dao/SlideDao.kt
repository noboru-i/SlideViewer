package hm.orz.chaos114.android.slideviewer.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.support.annotation.CheckResult
import hm.orz.chaos114.android.slideviewer.data.entities.SlideEntity
import io.reactivex.Flowable

@Dao
abstract class SlideDao {
    @CheckResult
    @Query("SELECT * FROM slide WHERE talk_id = :talkId")
    abstract fun findByTalkId(talkId: Int): Flowable<List<SlideEntity>>

    @Query("DELETE FROM slide WHERE talk_id = :talkId")
    abstract fun deleteByTalkId(talkId: Int)
}
