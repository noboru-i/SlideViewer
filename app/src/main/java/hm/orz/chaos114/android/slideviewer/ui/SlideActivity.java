package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySlideBinding;
import hm.orz.chaos114.android.slideviewer.infra.dao.TalkDao;
import hm.orz.chaos114.android.slideviewer.infra.model.Slide;
import hm.orz.chaos114.android.slideviewer.infra.model.Talk;
import hm.orz.chaos114.android.slideviewer.infra.model.TalkMetaData;
import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository;
import hm.orz.chaos114.android.slideviewer.infra.repository.TalkRepository;
import hm.orz.chaos114.android.slideviewer.ocr.OcrRecognizer;
import hm.orz.chaos114.android.slideviewer.util.AdRequestGenerator;
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager;
import hm.orz.chaos114.android.slideviewer.util.IntentUtil;
import hm.orz.chaos114.android.slideviewer.infra.network.SlideShareLoader;
import hm.orz.chaos114.android.slideviewer.util.UrlHelper;
import hm.orz.chaos114.android.slideviewer.widget.PickPageDialog;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SlideActivity extends AppCompatActivity {
    private static final String TAG = SlideActivity.class.getSimpleName();

    @Inject
    OcrRecognizer ocrRecognizer;
    @Inject
    TalkRepository talkRepository;
    @Inject
    SlideShareLoader loader;

    private ActivitySlideBinding binding;

    private Menu menu;
    private SlideAdapter adapter;
    private LoadingDialogFragment loadingDialog;
    private InterstitialAd interstitialAd;

    private Uri uri;
    private Talk talk;
    private Map<String, String> recognizeTextMap;
    private String currentLanguageId;

    static void start(Context context, @NonNull String url) {
        Intent intent = new Intent(context, SlideActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_slide);

        AnalyticsManager.sendScreenView(TAG);

        recognizeTextMap = new HashMap<>();

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
                // no-op
            }

            @Override
            public void onPageSelected(int position) {
                final int page = position + 1;
                setPageNumbers(page, talk.getSlides().size());
                AnalyticsManager.sendChangePageEvent(TAG, talk.getUrl(), page);

                setRecognizedText();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // no-op
            }
        });

        binding.prevButton.setOnClickListener(v -> movePrev());
        binding.nextButton.setOnClickListener(v -> moveNext());
        binding.slidePageNumbers.setOnClickListener(v -> {
            int current = binding.slideViewPager.getCurrentItem();
            int max = talk.getSlides().size();
            PickPageDialog.show(this, current, max, page -> binding.slideViewPager.setCurrentItem(page));
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

        ocrRecognizer.listen()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ocrResult -> {
                    Timber.d("original is: %s", ocrResult.getUrl());
                    Timber.d("text is recognized: %s", ocrResult.getRecognizedText());
                    recognizeTextMap.put(ocrResult.getUrl(), ocrResult.getRecognizedText());
                    setRecognizedText();
                });
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


        // reset text if language is changed.
        SettingsRepository settingsRepository = new SettingsRepository(this);
        binding.recognizeText.setVisibility(settingsRepository.getEnableOcr() ? View.VISIBLE : View.GONE);
        if (!settingsRepository.getEnableOcr()
                || currentLanguageId == null
                || !currentLanguageId.equals(settingsRepository.getSelectedLanguage())) {
            currentLanguageId = settingsRepository.getSelectedLanguage();
            // reset
            recognizeTextMap = new HashMap<>();
        }
        setRecognizedText();
        adapter.notifyDataSetChanged();
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
            case R.id.menu_setting:
                SettingActivity.start(this);
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
        talkRepository.findByUrl(uri.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MaybeObserver<Talk>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // no-op
                    }

                    @Override
                    public void onSuccess(Talk t) {
                        talk = t;
                        // DBにデータがある場合の描画処理
                        TalkMetaData talkMetaData = talk.getTalkMetaDataCollection().iterator().next();
                        binding.slideTitle.setText(talkMetaData.getTitle());
                        binding.slideUser.setText(talkMetaData.getUser());
                        setPageNumbers(1, talk.getSlides().size());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable e) {
                        // no-op
                    }

                    @Override
                    public void onComplete() {
                        loadingDialog.show(getFragmentManager(), null);

                        loader.load(uri)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(talkMetaData -> {
                                    binding.slideTitle.setText(talkMetaData.getTitle());
                                    binding.slideUser.setText(talkMetaData.getUser());

                                    if (talkMetaData.getTalk() != null) {
                                        AnalyticsManager.sendStartEvent(TAG, talkMetaData.getTalk().getUrl());

                                        talk = talkMetaData.getTalk();
                                        loadingDialog.dismiss();
                                        setPageNumbers(1, talk.getSlides().size());
                                        adapter.notifyDataSetChanged();
                                    }
                                }, throwable -> Toast.makeText(SlideActivity.this, "failed.", Toast.LENGTH_SHORT).show());
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
        binding.slidePageNumbers.setText(current + " / " + max);
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

    private void setRecognizedText() {
        if (talk == null) {
            return;
        }
        Slide slide = talk.getSlides().get(binding.slideViewPager.getCurrentItem());
        if (recognizeTextMap.containsKey(slide.getOriginal())) {
            binding.recognizeText.setText(recognizeTextMap.get(slide.getOriginal()));
        } else {
            binding.recognizeText.setText(R.string.recognizing);
        }
    }

    private void moveNext() {
        int current = binding.slideViewPager.getCurrentItem();
        binding.slideViewPager.setCurrentItem(current + 1, false);
    }

    private void movePrev() {
        int current = binding.slideViewPager.getCurrentItem();
        binding.slideViewPager.setCurrentItem(current - 1, false);
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
        public int getItemPosition(Object object) {
            // update when call notifyDataSetChanged.
            // http://stackoverflow.com/a/7287121
            return POSITION_NONE;
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
            refreshButton.setOnClickListener(v -> loadImage(slide, layout, position));
            imageView.setOnPhotoTapListener((imageView1, x, y) -> {
                if (x < 0.5f) {
                    movePrev();
                } else {
                    moveNext();
                }
            });

            loadImage(slide, layout, position);
            setRecognizedText();

            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        private void loadImage(Slide slide, View layout, int position) {
            Timber.d("loadImage: %d", position);
            final ProgressBar progressBar = (ProgressBar) layout.findViewById(R.id.slide_image_progress);
            final ImageView imageView = (ImageView) layout.findViewById(R.id.slide_image);
            final TextView refreshButton = (TextView) layout.findViewById(R.id.refresh_button);
            progressBar.setVisibility(View.VISIBLE);
            refreshButton.setVisibility(View.GONE);

            Glide.with(SlideActivity.this)
                    .asBitmap()
                    .load(slide.getOriginal())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            Timber.d("Glide onException");
                            progressBar.setVisibility(View.GONE);
                            refreshButton.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            Timber.d("Glide onResourceReady: %d", position);
                            if (recognizeTextMap.containsKey(slide.getOriginal())
                                    && position == binding.slideViewPager.getCurrentItem()) {
                                setRecognizedText();
                                Timber.d("onResourceReady: %d, contains and same position", position);
                                return false;
                            }
                            ocrRecognizer.recognize(slide.getOriginal(), resource);
                            return false;
                        }
                    })
                    .into(imageView);
        }
    }
}
