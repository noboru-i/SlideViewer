package hm.orz.chaos114.android.slideviewer.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class Slide implements Serializable {

    private String original;
    private String preview;
    private String thumb;
}
