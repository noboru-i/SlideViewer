package hm.orz.chaos114.android.slideviewer.infra.model

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable
data class Slide(
        @DatabaseField(generatedId = true)
        var id: Int = 0,

        @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "talk_id")
        var talk: Talk? = null,

        @DatabaseField
        var original: String? = null,

        @DatabaseField
        var preview: String? = null,

        @DatabaseField
        var thumb: String? = null
)
