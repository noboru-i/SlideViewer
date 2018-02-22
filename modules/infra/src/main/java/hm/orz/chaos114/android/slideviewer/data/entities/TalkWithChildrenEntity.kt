package hm.orz.chaos114.android.slideviewer.data.entities

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Relation

data class TalkWithChildrenEntity(
        @Embedded var talk: TalkEntity? = null,
        @Relation(parentColumn = "id", entityColumn = "talk_id") var slideList: List<SlideEntity>? = null,
        @Relation(parentColumn = "id", entityColumn = "talk_id") var talkMetaDataList: List<TalkMetaDataEntity>? = null
)