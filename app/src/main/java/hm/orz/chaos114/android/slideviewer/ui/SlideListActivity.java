package hm.orz.chaos114.android.slideviewer.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.google.android.gms.ads.AdRequest;

import java.util.List;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.dao.TalkDao;
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySlideListBinding;
import hm.orz.chaos114.android.slideviewer.model.Slide;
import hm.orz.chaos114.android.slideviewer.model.Talk;
import hm.orz.chaos114.android.slideviewer.model.TalkMetaData;
import hm.orz.chaos114.android.slideviewer.widget.SlideListRowView;

public class SlideListActivity extends AppCompatActivity {

    private ActivitySlideListBinding binding;
    private SlideListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_slide_list);

        setSupportActionBar(binding.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setTitle(R.string.slide_list_title);

        adapter = new SlideListAdapter();
        binding.list.setAdapter(adapter);
        binding.list.setEmptyView(binding.emptyLayout);
        binding.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onSlideClick(position);
            }
        });
        binding.emptyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSpeakerDeck();
            }
        });

        loadAd();
    }

    @Override
    protected void onPause() {
        binding.adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.adView.resume();

        TalkDao dao = new TalkDao(this);
        List<Talk> talks = dao.list();
        adapter.updateData(talks);
    }

    @Override
    protected void onDestroy() {
        binding.adView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.slide_list_activity_menus, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                openSpeakerDeck();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onSlideClick(int position) {
        Talk talk = adapter.getItem(position);
        SlideActivity.start(this, talk.getUrl());
    }

    private void openSpeakerDeck() {
        WebViewActivity.start(this, "https://speakerdeck.com/");
    }

    private void loadAd() {
        // TODO 共通化
        String testDeviceId = getString(R.string.admob_test_device);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(testDeviceId).build();
        binding.adView.loadAd(adRequest);
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
