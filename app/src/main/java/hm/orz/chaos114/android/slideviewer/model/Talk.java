package hm.orz.chaos114.android.slideviewer.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class Talk implements Serializable {
    private int modifiedAt;
    private String url;
    private float ratio;
    private List<Slide> slides;
}
