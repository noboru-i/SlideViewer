package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.dao.TalkDao;
import hm.orz.chaos114.android.slideviewer.model.Slide;
import hm.orz.chaos114.android.slideviewer.model.Talk;
import hm.orz.chaos114.android.slideviewer.model.TalkMetaData;
import hm.orz.chaos114.android.slideviewer.widget.SlideListRowView;

public class SlideListActivity extends AppCompatActivity {

    @Bind(R.id.slide_list_list_view)
    ListView mListView;
    @Bind(R.id.slide_list_ad_view)
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_list);

        ButterKnife.bind(this);

        TalkDao dao = new TalkDao(this);
        List<Talk> talks = dao.list();
        final SlideListAdapter adapter = new SlideListAdapter(talks);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Talk talk = adapter.getItem(position);
                Intent intent = new Intent(SlideListActivity.this, SlideActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(talk.getUrl()));
                startActivity(intent);
            }
        });

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
    }

    @Override
    protected void onDestroy() {
        mAdView.destroy();
        super.onDestroy();
    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("6B74A80630FD70AC2DC27C79CE02AEC9").build();
        mAdView.loadAd(adRequest);
    }

    private static class SlideListAdapter extends BaseAdapter {
        private List<Talk> mTalks;

        private SlideListAdapter(List<Talk> talks) {
            mTalks = talks;
        }

        @Override
        public int getCount() {
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
    }
}
