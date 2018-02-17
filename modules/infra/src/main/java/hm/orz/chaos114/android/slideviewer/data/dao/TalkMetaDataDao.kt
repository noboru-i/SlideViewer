package hm.orz.chaos114.android.slideviewer.data.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao abstract class TalkMetaDataDao {
    @Query("DELETE FROM talkmetadata WHERE talk_id = :talkId")
    abstract fun deleteByTalkId(talkId: Int)
}
