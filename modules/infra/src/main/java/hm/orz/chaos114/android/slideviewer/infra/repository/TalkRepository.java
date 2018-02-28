package hm.orz.chaos114.android.slideviewer.infra.repository;

import android.content.Context;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import hm.orz.chaos114.android.slideviewer.infra.model.Slide;
import hm.orz.chaos114.android.slideviewer.infra.model.Talk;
import hm.orz.chaos114.android.slideviewer.infra.util.DatabaseHelper;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;

public class TalkRepository {
    private Context mContext;

    public TalkRepository(Context context) {
        mContext = context;
    }

    public Maybe<Talk> findByUrl(final String url) {
        return Maybe.create(new MaybeOnSubscribe<Talk>() {
            @Override
            public void subscribe(MaybeEmitter<Talk> emitter) throws Exception {
                DatabaseHelper helper = new DatabaseHelper(mContext);
                try {
                    Dao<Talk, Integer> dao = helper.getDao(Talk.class);
                    List<Talk> talks = dao.queryForEq("url", url);
                    if (talks.isEmpty()) {
                        emitter.onComplete();
                        return;
                    }
                    Talk talk = talks.get(0);
                    setTalkInfo(talk);

                    emitter.onSuccess(talk);
                    emitter.onComplete();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } finally {
                    helper.close();
                }
            }
        });
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
