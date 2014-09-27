package hm.orz.chaos114.android.slideviewer.dao;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import hm.orz.chaos114.android.slideviewer.model.Talk;
import hm.orz.chaos114.android.slideviewer.util.DatabaseHelper;

public class TalkDao {
    private Context mContext;

    public TalkDao(Context context) {
        mContext = context;
    }

    public Talk findByUrl(String url) {
        DatabaseHelper helper = new DatabaseHelper(mContext);
        try {
            Dao<Talk, Integer> dao = helper.getDao(Talk.class);
            List<Talk> talks = dao.queryForEq("url", url);
            if (talks.isEmpty()) {
                return null;
            }
            return talks.get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            helper.close();
        }
    }

    public void saveIfNotExists(Talk talk) {
        DatabaseHelper helper = new DatabaseHelper(mContext);
        try {
            Dao<Talk, Integer> dao = helper.getDao(Talk.class);
            Talk oldTalk = findByUrl(talk.getUrl());
            if (oldTalk != null) {
                // 既に保存済み
                return;
            }
            dao.create(talk);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            helper.close();
        }
    }

    public List<Talk> list() {
        DatabaseHelper helper = new DatabaseHelper(mContext);
        try {
            Dao<Talk, Integer> dao = helper.getDao(Talk.class);
            return dao.queryForAll();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            helper.close();
        }
    }
}
