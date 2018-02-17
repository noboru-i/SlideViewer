package hm.orz.chaos114.android.slideviewer.data.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.PrimaryKey

data class TalkMetaData(
        @PrimaryKey var id: Int,
        @ColumnInfo(name = "talk_id") var talkId: Int,
        var title: String,
        var user: Float
)
