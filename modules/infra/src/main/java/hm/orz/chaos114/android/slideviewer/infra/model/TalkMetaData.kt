package hm.orz.chaos114.android.slideviewer.infra.model


import com.j256.ormlite.field.DatabaseField

data class TalkMetaData(
        @DatabaseField(generatedId = true)
        var id: Int? = null,

        @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "talk_id")
        var talk: Talk? = null,

        @DatabaseField
        var title: String? = null,

        @DatabaseField
        var user: String? = null
)
