package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySlideListBinding;
import hm.orz.chaos114.android.slideviewer.infra.model.Slide;
import hm.orz.chaos114.android.slideviewer.infra.model.Talk;
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData;
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository;
import hm.orz.chaos114.android.slideviewer.util.AdRequestGenerator;
import hm.orz.chaos114.android.slideviewer.widget.SlideListRowView;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SlideListActivity extends AppCompatActivity {

    @Inject
    TalkRepository talkRepository;

    private ActivitySlideListBinding binding;
    private SlideListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_slide_list);

        setSupportActionBar(binding.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setTitle(R.string.slide_list_title);

        adapter = new SlideListAdapter(talkRepository);
        binding.list.setAdapter(adapter);
        binding.list.setLayoutManager(new LinearLayoutManager(this));
        binding.emptyLayout.setOnClickListener(v -> openSpeakerDeck());

        binding.adView.loadAd(AdRequestGenerator.generate(this));
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

        talkRepository.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(talks -> {
                    if (talks.size() == 0) {
                        binding.emptyLayout.setVisibility(View.VISIBLE);
                        binding.list.setVisibility(View.GONE);
                    } else {
                        binding.emptyLayout.setVisibility(View.GONE);
                        binding.list.setVisibility(View.VISIBLE);
                    }
                    adapter.updateData(talks);
                });
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
            case R.id.menu_share:
                shareUrl();
                return true;
            case R.id.menu_about:
                AboutActivity.start(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareUrl() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=hm.orz.chaos114.android.slideviewer");
        startActivity(intent);
    }

    private void openSpeakerDeck() {
        WebViewActivity.start(this, "https://speakerdeck.com/");
    }

    private static class SlideListAdapter extends RecyclerView.Adapter<ViewHolder> {
        private TalkRepository talkRepository;
        private List<Talk> mTalks;

        private SlideListAdapter(TalkRepository talkRepository) {
            this.talkRepository = talkRepository;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SlideListRowView view = new SlideListRowView(parent.getContext());
            view.setOnClickListener(v -> onClick(parent.getContext(), (Talk) view.getTag()));
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Talk item = mTalks.get(position);
            List<Slide> slides = item.getSlides();
            TalkMetaData talkMetaData = talkRepository.findMetaData(item);

            ((SlideListRowView) holder.itemView).bind(slides, talkMetaData);
            holder.itemView.setTag(talkMetaData.getTalk());
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            if (mTalks == null) {
                return 0;
            }
            return mTalks.size();
        }

        void updateData(List<Talk> talks) {
            mTalks = talks;
            notifyDataSetChanged();
        }

        private void onClick(Context context, Talk talk) {
            SlideActivity.start(context, talk.getUrl());
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
