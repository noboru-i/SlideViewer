package hm.orz.chaos114.android.slideviewer.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.dao.TalkDao;
import hm.orz.chaos114.android.slideviewer.model.Slide;
import hm.orz.chaos114.android.slideviewer.model.Talk;
import hm.orz.chaos114.android.slideviewer.model.TalkMetaData;
import hm.orz.chaos114.android.slideviewer.widget.SlideListRowView;

public class SlideListActivity extends AppCompatActivity {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.slide_list_list_view)
    ListView mListView;
    @Bind(R.id.layout_empty)
    View mEmptyView;
    @Bind(R.id.slide_list_ad_view)
    AdView mAdView;

    private SlideListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_list);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle(R.string.slide_list_title);

        adapter = new SlideListAdapter();
        mListView.setAdapter(adapter);
        mListView.setEmptyView(mEmptyView);

        loadAd();
    }

    @Override
    protected void onPause() {
        mAdView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();

        TalkDao dao = new TalkDao(this);
        List<Talk> talks = dao.list();
        adapter.updateData(talks);
    }

    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }

    @OnItemClick(R.id.slide_list_list_view)
    void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Talk talk = adapter.getItem(position);
        SlideActivity.start(this, talk.getUrl());
    }

    @OnClick(R.id.layout_empty)
    void onClickEmpty() {
        WebViewActivity.start(this, "https://speakerdeck.com/");
    }

    private void loadAd() {
        // TODO 共通化
        String testDeviceId = getString(R.string.admob_test_device);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(testDeviceId).build();
        mAdView.loadAd(adRequest);
    }

    private static class SlideListAdapter extends BaseAdapter {
        private List<Talk> mTalks;

        private SlideListAdapter() {
            // no-op
        }

        @Override
        public int getCount() {
            if (mTalks == null) {
                return 0;
            }
            return mTalks.size();
        }

        @Override
        public Talk getItem(int position) {
            return mTalks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public SlideListRowView getView(int position, View convertView, ViewGroup parent) {
            SlideListRowView v = (SlideListRowView) convertView;
            if (v == null) {
                v = new SlideListRowView(parent.getContext());
            }
            Talk item = getItem(position);
            List<Slide> slides = item.getSlides();
            TalkDao dao = new TalkDao(v.getContext());
            TalkMetaData talkMetaData = dao.findMetaData(item);

            v.bind(slides, talkMetaData);

            return v;
        }

        void updateData(List<Talk> talks) {
            mTalks = talks;
            notifyDataSetChanged();
        }
    }
}
