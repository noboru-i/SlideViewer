package hm.orz.chaos114.android.slideviewer.data.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "talk")
data class TalkEntity(
        @PrimaryKey var id: Int,
        var modifiedAt: Int,
        var url: String,
        var ratio: Float
)