package hm.orz.chaos114.android.slideviewer.infra.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import lombok.Data;

@Data
@DatabaseTable
public class Slide implements Serializable {
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "talk_id")
    private Talk talk;

    @DatabaseField
    private String original;

    @DatabaseField
    private String preview;

    @DatabaseField
    private String thumb;
}
