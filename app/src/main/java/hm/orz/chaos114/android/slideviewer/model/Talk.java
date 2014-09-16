package hm.orz.chaos114.android.slideviewer.model;

import java.io.Serializable;
import java.util.List;

public class Talk implements Serializable {
    private int modifiedAt;
    private String url;
    private float ratio;
    private List<Slide> slides;

    public int getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(int modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    public List<Slide> getSlides() {
        return slides;
    }

    public void setSlides(List<Slide> slides) {
        this.slides = slides;
    }

    @Override
    public String toString() {
        return "Talk{" +
                "modifiedAt=" + modifiedAt +
                ", url='" + url + '\'' +
                ", ratio=" + ratio +
                ", slides=" + slides +
                '}';
    }
}
