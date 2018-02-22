package hm.orz.chaos114.android.slideviewer.data.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "talkmetadata")
data class TalkMetaDataEntity(
        @PrimaryKey var id: Int,
        @ColumnInfo(name = "talk_id") var talkId: Int,
        var title: String,
        var user: String
)
