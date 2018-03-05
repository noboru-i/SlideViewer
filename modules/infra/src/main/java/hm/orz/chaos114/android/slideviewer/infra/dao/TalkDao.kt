package hm.orz.chaos114.android.slideviewer.infra.dao

import android.content.Context
import com.j256.ormlite.dao.Dao
import hm.orz.chaos114.android.slideviewer.infra.model.Slide
import hm.orz.chaos114.android.slideviewer.infra.model.Talk
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData
import hm.orz.chaos114.android.slideviewer.infra.util.DatabaseHelper
import java.io.IOException
import java.sql.SQLException

class TalkDao(private val mContext: Context) {

    private fun findByUrl(url: String?): Talk? {
        val helper = DatabaseHelper(mContext)
        try {
            val dao = helper.getDao<Dao<Talk, Int>, Talk>(Talk::class.java)
            val talks = dao.queryForEq("url", url)
            if (talks.isEmpty()) {
                return null
            }
            val talk = talks[0]
            setTalkInfo(talk)

            return talk
        } catch (e: SQLException) {
            throw RuntimeException(e)
        } finally {
            helper.close()
        }
    }

    fun deleteByUrl(url: String) {
        val helper = DatabaseHelper(mContext)
        try {
            val talkDao = helper.getDao<Dao<Talk, Int>, Talk>(Talk::class.java)
            val slideDao = helper.getDao<Dao<Slide, Int>, Slide>(Slide::class.java)
            val talkMetaDataDao = helper.getDao<Dao<TalkMetaData, Int>, TalkMetaData>(TalkMetaData::class.java)
            val oldTalk = findByUrl(url) ?: return
            setTalkInfo(oldTalk)
            val meta = findMetaData(oldTalk)
            talkDao.deleteById(oldTalk.id)
            slideDao.delete(oldTalk.slides)
            talkMetaDataDao.delete(meta)
        } catch (e: SQLException) {
            throw RuntimeException(e)
        } finally {
            helper.close()
        }
    }

    fun findMetaData(talk: Talk): TalkMetaData? {
        val helper = DatabaseHelper(mContext)
        try {
            val dao = helper.getDao<Dao<TalkMetaData, Int>, TalkMetaData>(TalkMetaData::class.java)
            val talkMetaDataList = dao.queryForEq("talk_id", talk.id)
            return if (talkMetaDataList.isEmpty()) {
                null
            } else talkMetaDataList[0]
        } catch (e: SQLException) {
            throw RuntimeException(e)
        } finally {
            helper.close()
        }
    }

    fun saveIfNotExists(talk: Talk, slides: List<Slide>, talkMetaData: TalkMetaData) {
        val helper = DatabaseHelper(mContext)
        try {
            val talkDao = helper.getDao<Dao<Talk, Int>, Talk>(Talk::class.java)
            val slideDao = helper.getDao<Dao<Slide, Int>, Slide>(Slide::class.java)
            val talkMetaDataDao = helper.getDao<Dao<TalkMetaData, Int>, TalkMetaData>(TalkMetaData::class.java)
            val oldTalk = findByUrl(talk.url)
            if (oldTalk != null) {
                // 既に保存済み
                return
            }
            talkDao.create(talk)
            for (slide in slides) {
                slide.talk = talk
                slideDao.create(slide)
            }
            talkMetaData.talk = talk
            talkMetaDataDao.create(talkMetaData)
        } catch (e: SQLException) {
            throw RuntimeException(e)
        } finally {
            helper.close()
        }
    }

    private fun setTalkInfo(talk: Talk) {
        val iterator = talk.slideCollection!!.closeableIterator()
        try {
            val slides = ArrayList<Slide>()
            while (iterator.hasNext()) {
                val slide = iterator.next()
                slides.add(slide)
            }
            talk.slides = slides
        } finally {
            try {
                iterator.close()
            } catch (e: IOException) {
                // no-op
            }

        }
    }
}
