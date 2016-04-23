package hm.orz.chaos114.android.slideviewer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

public class SlideListActivity extends Activity {

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
        View footer = getLayoutInflater().inflate(R.layout.footer, null);
        mListView.addFooterView(footer);
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

    class SlideListAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private List<Talk> mTalks;

        SlideListAdapter(List<Talk> talks) {
            mTalks = talks;
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            Talk item = getItem(position);
            List<Slide> slides = item.getSlides();
            TalkDao dao = new TalkDao(SlideListActivity.this);
            TalkMetaData talkMetaData = dao.findMetaData(item);
            if (v == null) {
                v = mInflater.inflate(R.layout.slide_list_row, null);
            }

            ImageView slideImage = (ImageView) v.findViewById(R.id.slide_list_row_image);
            Glide.with(SlideListActivity.this).load(slides.get(0).getPreview()).into(slideImage);
            if (talkMetaData != null) {
                TextView titleView = (TextView) v.findViewById(R.id.slide_list_row_title);
                titleView.setText(talkMetaData.getTitle());
                TextView userView = (TextView) v.findViewById(R.id.slide_list_row_user);
                userView.setText("by " + talkMetaData.getUser());
            }

            return v;
        }
    }
}
