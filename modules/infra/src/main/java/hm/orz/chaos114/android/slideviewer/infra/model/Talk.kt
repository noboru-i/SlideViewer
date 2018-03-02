package hm.orz.chaos114.android.slideviewer.infra.model

import com.google.gson.annotations.Expose
import com.j256.ormlite.dao.ForeignCollection
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.field.ForeignCollectionField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable
data class Talk(

        @DatabaseField(generatedId = true)
        var id: Int? = null,

        @DatabaseField
        var modifiedAt: Int = 0,

        @DatabaseField
        var url: String? = null,

        @DatabaseField
        var ratio: Float = 0.toFloat(),

        @Expose
        @ForeignCollectionField(eager = true)
        var slideCollection: ForeignCollection<Slide>? = null,

        @Expose
        @ForeignCollectionField(eager = true)
        var talkMetaData: ForeignCollection<TalkMetaData>? = null,

        var slides: List<Slide>? = null

) {
    val talkMetaDataCollection: Collection<TalkMetaData>
        get() = this.talkMetaData as Collection<TalkMetaData>
}
