package hm.orz.chaos114.android.slideviewer.infra.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import hm.orz.chaos114.android.slideviewer.infra.model.Slide;
import hm.orz.chaos114.android.slideviewer.infra.model.Talk;
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData;
import timber.log.Timber;

public final class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "slide_viewer.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Talk.class);
            TableUtils.createTable(connectionSource, Slide.class);
            TableUtils.createTable(connectionSource, TalkMetaData.class);
        } catch (SQLException e) {
            Timber.e(e, "データベースを作成できませんでした。");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i2) {
    }
}
