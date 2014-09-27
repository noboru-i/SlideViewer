package hm.orz.chaos114.android.slideviewer.model;

import com.google.gson.Gson;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.List;

import hm.orz.chaos114.android.slideviewer.util.DatabaseHelper;
import lombok.Data;

@Data
public class Talk implements Serializable {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField
    private int modifiedAt;

    @DatabaseField
    private String url;

    @DatabaseField
    private float ratio;

    private List<Slide> slides;

    @DatabaseField
    private String slidesJson;

    public void setSlides(List<Slide> slides) {
        this.slides = slides;

        Gson gson = new Gson();
        slidesJson = gson.toJson(slides);
    }

    public void setSlidesJson(String slidesJson) {
        this.slidesJson = slidesJson;

        Gson gson = new Gson();
        this.slides = gson.fromJson(slidesJson, List.class);
    }
}
