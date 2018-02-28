package hm.orz.chaos114.android.slideviewer.infra.dao;

import android.content.Context;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hm.orz.chaos114.android.slideviewer.infra.model.Slide;
import hm.orz.chaos114.android.slideviewer.infra.model.Talk;
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData;
import hm.orz.chaos114.android.slideviewer.infra.util.DatabaseHelper;

public class TalkDao {
    private Context mContext;

    public TalkDao(Context context) {
        mContext = context;
    }

    private Talk findByUrl(String url) {
        DatabaseHelper helper = new DatabaseHelper(mContext);
        try {
            Dao<Talk, Integer> dao = helper.getDao(Talk.class);
            List<Talk> talks = dao.queryForEq("url", url);
            if (talks.isEmpty()) {
                return null;
            }
            Talk talk = talks.get(0);
            setTalkInfo(talk);

            return talk;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            helper.close();
        }
    }

    public void deleteByUrl(String url) {
        DatabaseHelper helper = new DatabaseHelper(mContext);
        try {
            Dao<Talk, Integer> talkDao = helper.getDao(Talk.class);
            Dao<Slide, Integer> slideDao = helper.getDao(Slide.class);
            Dao<TalkMetaData, Integer> talkMetaDataDao = helper.getDao(TalkMetaData.class);
            Talk oldTalk = findByUrl(url);
            if (oldTalk == null) {
                return;
            }
            setTalkInfo(oldTalk);
            TalkMetaData meta = findMetaData(oldTalk);
            talkDao.deleteById(oldTalk.getId());
            slideDao.delete(oldTalk.getSlides());
            talkMetaDataDao.delete(meta);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            helper.close();
        }
    }

    public TalkMetaData findMetaData(Talk talk) {
        DatabaseHelper helper = new DatabaseHelper(mContext);
        try {
            Dao<TalkMetaData, Integer> dao = helper.getDao(TalkMetaData.class);
            List<TalkMetaData> talkMetaDataList = dao.queryForEq("talk_id", talk.getId());
            if (talkMetaDataList.isEmpty()) {
                return null;
            }
            return talkMetaDataList.get(0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            helper.close();
        }
    }

    public void saveIfNotExists(Talk talk, List<Slide> slides, TalkMetaData talkMetaData) {
        DatabaseHelper helper = new DatabaseHelper(mContext);
        try {
            Dao<Talk, Integer> talkDao = helper.getDao(Talk.class);
            Dao<Slide, Integer> slideDao = helper.getDao(Slide.class);
            Dao<TalkMetaData, Integer> talkMetaDataDao = helper.getDao(TalkMetaData.class);
            Talk oldTalk = findByUrl(talk.getUrl());
            if (oldTalk != null) {
                // 既に保存済み
                return;
            }
            talkDao.create(talk);
            for (Slide slide : slides) {
                slide.setTalk(talk);
                slideDao.create(slide);
            }
            talkMetaData.setTalk(talk);
            talkMetaDataDao.create(talkMetaData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            helper.close();
        }
    }

    private void setTalkInfo(Talk talk) {
        CloseableIterator<Slide> iterator = talk.getSlideCollection().closeableIterator();
        try {
            List<Slide> slides = new ArrayList<>();
            while (iterator.hasNext()) {
                Slide slide = iterator.next();
                slides.add(slide);
            }
            talk.setSlides(slides);
        } finally {
            try {
                iterator.close();
            } catch (IOException e) {
                // no-op
            }
        }
    }
}
