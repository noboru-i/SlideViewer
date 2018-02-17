package hm.orz.chaos114.android.slideviewer.data.entities

import android.arch.persistence.room.PrimaryKey

data class TalkEntity(
        @PrimaryKey var id: Int,
        var modifiedAt: Int,
        var url: String,
        var ratio: Float
)