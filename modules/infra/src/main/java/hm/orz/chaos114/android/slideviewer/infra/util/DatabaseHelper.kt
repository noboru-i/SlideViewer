package hm.orz.chaos114.android.slideviewer.infra.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils

import java.sql.SQLException

import hm.orz.chaos114.android.slideviewer.infra.model.Slide
import hm.orz.chaos114.android.slideviewer.infra.model.Talk
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData
import timber.log.Timber

class DatabaseHelper(context: Context) : OrmLiteSqliteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase, connectionSource: ConnectionSource) {
        try {
            TableUtils.createTable<Talk>(connectionSource, Talk::class.java)
            TableUtils.createTable<Slide>(connectionSource, Slide::class.java)
            TableUtils.createTable<TalkMetaData>(connectionSource, TalkMetaData::class.java)
        } catch (e: SQLException) {
            Timber.e(e, "データベースを作成できませんでした。")
        }
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, connectionSource: ConnectionSource, i: Int, i2: Int) {}

    companion object {
        private val DATABASE_NAME = "slide_viewer.db"
        private val DATABASE_VERSION = 1
    }
}
