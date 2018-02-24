package hm.orz.chaos114.android.slideviewer.infra.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import lombok.Data;
import lombok.ToString;

@Data
@DatabaseTable
@ToString(exclude = {"slideCollection", "talkMetaData", "slides"})
public class Talk implements Serializable {

    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField
    private int modifiedAt;

    @DatabaseField
    private String url;

    @DatabaseField
    private float ratio;

    @Expose
    @ForeignCollectionField(eager = true)
    private ForeignCollection<Slide> slideCollection;

    @Expose
    @ForeignCollectionField(eager = true)
    private ForeignCollection<TalkMetaData> talkMetaData;

    private List<Slide> slides;

    public Collection<TalkMetaData> getTalkMetaDataCollection() {
        return talkMetaData;
    }
}
