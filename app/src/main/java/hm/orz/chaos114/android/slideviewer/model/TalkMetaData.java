package hm.orz.chaos114.android.slideviewer.model;


import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

import lombok.Data;

@Data
public class TalkMetaData {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "talk_id")
    private Talk talk;

    @DatabaseField
    private Date createdAt;

    @DatabaseField
    private Date updatedAt;

    @DatabaseField
    private Date recentViewedAt;

    @DatabaseField
    private String title;

    @DatabaseField
    private String user;
}