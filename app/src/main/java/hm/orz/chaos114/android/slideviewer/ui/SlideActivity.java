package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.dao.TalkDao;
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySlideBinding;
import hm.orz.chaos114.android.slideviewer.model.Slide;
import hm.orz.chaos114.android.slideviewer.model.Talk;
import hm.orz.chaos114.android.slideviewer.model.TalkMetaData;
import hm.orz.chaos114.android.slideviewer.util.AdRequestGenerator;
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager;
import hm.orz.chaos114.android.slideviewer.util.IntentUtil;
import hm.orz.chaos114.android.slideviewer.util.UrlHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class SlideActivity extends AppCompatActivity {
    private static final String TAG = SlideActivity.class.getSimpleName();

    private Handler handler;

    private ActivitySlideBinding binding;

    private Menu menu;
    private SlideAdapter adapter;
    private LoadingDialogFragment loadingDialog;
    private InterstitialAd interstitialAd;

    private Uri uri;
    private Talk talk;
    private TalkMetaData talkMetaData;

    static void start(Context context, @NonNull String url) {
        Intent intent = new Intent(context, SlideActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsManager.initializeAnalyticsTracker(getApplication());
        binding = DataBindingUtil.setContentView(this, R.layout.activity_slide);

        AnalyticsManager.sendScreenView(TAG);

        handler = new Handler();

        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() == null) {
            throw new AssertionError("getSupportActionBar() needs non-null.");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        adapter = new SlideAdapter(this);
        loadingDialog = LoadingDialogFragment.newInstance();

        binding.slideViewPager.setOffscreenPageLimit(5);
        binding.slideViewPager.setAdapter(adapter);
        binding.slideViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                final int page = position + 1;
                setPageNumbers(page, talk.getSlides().size());
                AnalyticsManager.sendEvent(TAG, AnalyticsManager.Action.CHANGE_PAGE.name(), Integer.toString(page));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            Timber.d("uri = %s", uri.toString());
            this.uri = uri;
        } else if (Intent.ACTION_SEND.equals(action)) {
            String uri = intent.getStringExtra(Intent.EXTRA_TEXT);
            Timber.d("uri = %s", uri);
            this.uri = Uri.parse(uri);
        } else {
            throw new RuntimeException("invalid intent.");
        }
        if (!UrlHelper.isSpeakerDeckUrl(uri)) {
            Toast.makeText(this, "Unsupported url.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!UrlHelper.canOpen(uri)) {
            WebViewActivity.start(this, uri.toString());
            finish();
            return;
        }

        loadAd();
        startLoad(false);
    }

    @Override
    protected void onPause() {
        binding.slideAdView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.slideAdView.resume();
    }

    @Override
    protected void onDestroy() {
        binding.slideAdView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.slide_activity_menus, menu);
        this.menu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                    // Menuボタン押下
                    if (menu != null) {
                        menu.performIdentifierAction(R.id.overflow_options, 0);
                    }
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        beforeFinish();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                beforeFinish();
                finish();
                return true;
            case R.id.menu_reload:
                binding.slideViewPager.setCurrentItem(0);
                startLoad(true);
                return true;
            case R.id.menu_share:
                shareUrl();
                return true;
            case R.id.menu_show_by_browser:
                shareBrowser();
                return true;
            case R.id.menu_about:
                startAboutActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void beforeFinish() {
        // start list activity
        Intent intent = new Intent(this, SlideListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        displayInterstitial();
    }

    private void loadAd() {
        AdRequest adRequest = AdRequestGenerator.generate(this);
        binding.slideAdView.loadAd(adRequest);

        // インタースティシャルを作成する。
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.admob_unit_id));
        interstitialAd.loadAd(adRequest);
    }

    private void startLoad(boolean refresh) {
        TalkDao dao = new TalkDao(this);
        if (refresh) {
            talk = null;
            adapter.notifyDataSetChanged();
            dao.deleteByUrl(uri.toString());
        }
        talk = dao.findByUrl(uri.toString());
        if (talk != null) {
            // DBにデータがある場合の描画処理
            TalkMetaData talkMetaData = talk.getTalkMetaData().iterator().next();
            binding.slideTitle.setText(talkMetaData.getTitle());
            binding.slideUser.setText(talkMetaData.getUser());
            setPageNumbers(1, talk.getSlides().size());
            adapter.notifyDataSetChanged();
            return;
        }

        loadingDialog.show(getFragmentManager(), null);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(uri.toString())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Timber.d(e, "onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    Document document = Jsoup.parse(response.body().string());

                    String dataId = document.select("div.speakerdeck-embed").get(0).attr("data-id");
                    Timber.d("dataId = %s", dataId);
                    String url = "https://speakerdeck.com/player/" + dataId + "?";
                    Timber.d("src = %s", url);
                    String title = document.select("#talk-details header h1").get(0).text();
                    Timber.d("title = %s", title);
                    String user = document.select("#talk-details header h2 a").get(0).text();
                    Timber.d("user = %s", user);

                    talkMetaData = new TalkMetaData();
                    talkMetaData.setTitle(title);
                    talkMetaData.setUser(user);
                    handler.post(() -> {
                        binding.slideTitle.setText(title);
                        binding.slideUser.setText(user);
                    });

                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Timber.d(e, "onFailure");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                Pattern pattern = Pattern.compile("var talk = ([^;]*)");
                                Matcher matcher = pattern.matcher(response.body().string());
                                if (matcher.find()) {
                                    Timber.d("group = %s", matcher.group(1));
                                    Gson gson = new GsonBuilder()
                                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                                            .create();
                                    talk = gson.fromJson(matcher.group(1), Talk.class);
                                    Timber.d("talkObject = %s", talk);
                                } else {
                                    Timber.d("not match");
                                }

                                AnalyticsManager.sendEvent(TAG, AnalyticsManager.Action.START.name(), talk.getUrl());

                                TalkDao dao = new TalkDao(SlideActivity.this);
                                dao.saveIfNotExists(talk, talk.getSlides(), talkMetaData);

                                handler.post(() -> {
                                    loadingDialog.dismiss();
                                    setPageNumbers(1, talk.getSlides().size());
                                    adapter.notifyDataSetChanged();
                                });


                            } catch (Exception e) {
                                Timber.e(e);
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (Exception e) {
                    Timber.e(e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * インタースティシャルを表示する準備が出来ていたら表示する。
     */
    public void displayInterstitial() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
    }

    private void setPageNumbers(int current, int max) {
        binding.slidePageNumbers.setText(current + "/" + max);
    }

    private void shareUrl() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
        startActivity(intent);
    }

    private void shareBrowser() {
        IntentUtil.browse(this, uri);
    }

    private void startAboutActivity() {
        AboutActivity.start(this);
    }

    private void toggleInfo() {
        binding.layoutInfo.setVisibility(binding.layoutInfo.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private class SlideAdapter extends PagerAdapter {
        private final LayoutInflater inflater;

        SlideAdapter(Context c) {
            super();
            inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (talk == null) {
                return 0;
            }
            return talk.getSlides().size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view.equals(o);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Slide slide = talk.getSlides().get(position);
            final View layout = inflater.inflate(R.layout.view_slide, container, false);
            final PhotoView imageView = (PhotoView) layout.findViewById(R.id.slide_image);
            final TextView refreshButton = (TextView) layout.findViewById(R.id.refresh_button);
            refreshButton.setOnClickListener(v -> loadImage(slide, layout));
            imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    toggleInfo();
                }

                @Override
                public void onOutsidePhotoTap() {

                }
            });

            loadImage(slide, layout);

            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        private void loadImage(Slide slide, View layout) {
            final ProgressBar progressBar = (ProgressBar) layout.findViewById(R.id.slide_image_progress);
            final ImageView imageView = (ImageView) layout.findViewById(R.id.slide_image);
            final TextView refreshButton = (TextView) layout.findViewById(R.id.refresh_button);
            progressBar.setVisibility(View.VISIBLE);
            refreshButton.setVisibility(View.GONE);

            Glide.with(SlideActivity.this)
                    .load(slide.getOriginal())
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            refreshButton.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            // no-op
                            return false;
                        }
                    })
                    .into(imageView);
        }
    }
}
