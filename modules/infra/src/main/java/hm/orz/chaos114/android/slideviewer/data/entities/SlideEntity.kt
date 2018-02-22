package hm.orz.chaos114.android.slideviewer.data.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "slide")
data class SlideEntity(
        @PrimaryKey var id: Int,
        @ColumnInfo(name = "talk_id") var talkId: Int,
        var original: String,
        var preview: String,
        var thumb: String
)
