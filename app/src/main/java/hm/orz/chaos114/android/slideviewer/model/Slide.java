package hm.orz.chaos114.android.slideviewer.model;

import java.io.Serializable;

public class Slide implements Serializable {

    private String original;
    private String preview;
    private String thumb;

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    @Override
    public String toString() {
        return "Slide{" +
                "original='" + original + '\'' +
                ", preview='" + preview + '\'' +
                ", thumb='" + thumb + '\'' +
                '}';
    }
}
