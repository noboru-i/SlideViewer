package hm.orz.chaos114.android.slideviewer.infra.repository

import android.content.Context
import com.j256.ormlite.dao.Dao
import hm.orz.chaos114.android.slideviewer.infra.dao.TalkDao
import hm.orz.chaos114.android.slideviewer.infra.model.Slide
import hm.orz.chaos114.android.slideviewer.infra.model.Talk
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData
import hm.orz.chaos114.android.slideviewer.infra.util.DatabaseHelper
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.MaybeOnSubscribe
import java.io.IOException
import java.sql.SQLException
import java.util.*

class TalkRepository(private val mContext: Context) {

    fun findByUrl(url: String): Maybe<Talk> {
        return Maybe.create(MaybeOnSubscribe { emitter ->
            val helper = DatabaseHelper(mContext)
            try {
                val dao = helper.getDao<Dao<Talk, Int>, Talk>(Talk::class.java)
                val talks = dao.queryForEq("url", url)
                if (talks.isEmpty()) {
                    emitter.onComplete()
                    return@MaybeOnSubscribe
                }
                val talk = talks[0]
                setTalkInfo(talk)

                emitter.onSuccess(talk)
                emitter.onComplete()
            } catch (e: SQLException) {
                throw RuntimeException(e)
            } finally {
                helper.close()
            }
        })
    }

    fun list(): Flowable<List<Talk>> {
        return Flowable.create({ emitter ->
            val helper = DatabaseHelper(mContext)
            try {
                val dao = helper.getDao<Dao<Talk, Int>, Talk>(Talk::class.java)
                val talks = dao.query(dao.queryBuilder().orderBy("id", false).limit(50L).prepare())
                for (talk in talks) {
                    setTalkInfo(talk)
                }
                emitter.onNext(talks)
            } catch (e: SQLException) {
                throw RuntimeException(e)
            } finally {
                helper.close()
            }
        }, BackpressureStrategy.LATEST)
    }

    fun count(): Long {
        val helper = DatabaseHelper(mContext)
        try {
            val dao = helper.getDao<Dao<Talk, Int>, Talk>(Talk::class.java)
            return dao.countOf()
        } catch (e: SQLException) {
            throw RuntimeException(e)
        } finally {
            helper.close()
        }
    }

    fun deleteByUrl(url: String) {
        val dao = TalkDao(mContext)
        dao.deleteByUrl(url)
    }

    fun findMetaData(talk: Talk): TalkMetaData? {
        val dao = TalkDao(mContext)
        return dao.findMetaData(talk)
    }

    fun saveIfNotExists(talk: Talk, slides: List<Slide>, talkMetaData: TalkMetaData) {
        val dao = TalkDao(mContext)
        dao.saveIfNotExists(talk, slides, talkMetaData)
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
