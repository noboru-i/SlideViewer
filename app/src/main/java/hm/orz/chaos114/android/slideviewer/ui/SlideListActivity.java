package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.dao.TalkDao;
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySlideListBinding;
import hm.orz.chaos114.android.slideviewer.model.Slide;
import hm.orz.chaos114.android.slideviewer.model.Talk;
import hm.orz.chaos114.android.slideviewer.model.TalkMetaData;
import hm.orz.chaos114.android.slideviewer.util.AdRequestGenerator;
import hm.orz.chaos114.android.slideviewer.widget.SlideListRowView;
import timber.log.Timber;

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

        TalkDao dao = new TalkDao(this);
        List<Talk> talks = dao.list();
        if (talks.size() == 0) {
            binding.emptyLayout.setVisibility(View.VISIBLE);
            binding.list.setVisibility(View.GONE);
        } else {
            binding.emptyLayout.setVisibility(View.GONE);
            binding.list.setVisibility(View.VISIBLE);
        }
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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Timber.d("remove 1");
            menu.removeItem(R.id.sign_out);
        } else {
            Timber.d("remove 2");
            menu.removeItem(R.id.sign_up);
        }

        Timber.d("menu %s", menu);
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
            case R.id.sign_up:
                startActivityForResult(
                        // Get an instance of AuthUI based on the default app
                        AuthUI.getInstance().createSignInIntentBuilder().build(),
                        123);
                return true;
            case R.id.sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(task -> Toast.makeText(SlideListActivity.this, "sign out", Toast.LENGTH_SHORT).show());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                Toast.makeText(this, "Welcome " + auth.getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Timber.d("cancelled");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Timber.d("no_internet_connection");
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Timber.d("unknown_error");
                    return;
                }
            }

            Timber.d("unknown_sign_in_response");
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
        private List<Talk> mTalks;

        private SlideListAdapter() {
            // no-op
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
            TalkDao dao = new TalkDao(holder.itemView.getContext());
            TalkMetaData talkMetaData = dao.findMetaData(item);

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
