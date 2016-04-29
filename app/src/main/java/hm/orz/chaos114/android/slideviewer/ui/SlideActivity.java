package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import butterknife.Bind;
import butterknife.ButterKnife;
import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.dao.TalkDao;
import hm.orz.chaos114.android.slideviewer.model.Slide;
import hm.orz.chaos114.android.slideviewer.model.Talk;
import hm.orz.chaos114.android.slideviewer.model.TalkMetaData;
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager;
import hm.orz.chaos114.android.slideviewer.util.UrlHelper;

public class SlideActivity extends AppCompatActivity {
    private static final String TAG = SlideActivity.class.getSimpleName();

    private Handler mHandler;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.slide_web_view)
    WebView mWebView;
    @Bind(R.id.layout_info)
    View mInfoLayout;
    @Bind(R.id.slide_title)
    TextView mTitleView;
    @Bind(R.id.slide_user)
    TextView mUserView;
    @Bind(R.id.slide_view_pager)
    ViewPager mViewPager;
    @Bind(R.id.slide_page_numbers)
    TextView mPageNumbers;
    @Bind(R.id.slide_ad_view)
    AdView mAdView;

    private Menu mMainMenu;
    private SlideAdapter mSlideAdapter;
    private LoadingDialogFragment mLoadingDialog;
    private InterstitialAd mInterstitialAd;

    private Uri mUrl;
    private Talk mTalk;
    private TalkMetaData mTalkMetaData;

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
        setContentView(R.layout.activity_slide);

        AnalyticsManager.sendScreenView(TAG);

        mHandler = new Handler();

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mSlideAdapter = new SlideAdapter(this);
        mLoadingDialog = LoadingDialogFragment.newInstance();

        mViewPager.setOffscreenPageLimit(5);
        mViewPager.setAdapter(mSlideAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                final int page = position + 1;
                setPageNumbers(page, mTalk.getSlides().size());
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
            Log.d(TAG, "uri = " + uri.toString());
            mUrl = uri;
        } else if (Intent.ACTION_SEND.equals(action)) {
            String uri = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.d(TAG, "uri = " + uri);
            mUrl = Uri.parse(uri);
        } else {
            throw new RuntimeException("invalid intent.");
        }
        if (!UrlHelper.isSpeakerDeckUrl(mUrl)) {
            Toast.makeText(this, "Unsupported url.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!UrlHelper.canOpen(mUrl)) {
            WebViewActivity.start(this, mUrl.toString());
            finish();
            return;
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.addJavascriptInterface(new SrcHolderInterface(), "srcHolder");
        mWebView.setWebViewClient(new MyWebViewClient());

        loadAd();
        startLoad();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.slide_activity_menus, menu);
        mMainMenu = menu;

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                    // Menuボタン押下
                    if (mMainMenu != null) {
                        mMainMenu.performIdentifierAction(R.id.overflow_options, 0);
                    }
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        displayInterstitial();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reload:
                mViewPager.setCurrentItem(0);
                startLoad();
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

    private void loadAd() {
        // TODO 共通化
        String testDeviceId = getString(R.string.admob_test_device);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(testDeviceId).build();
        mAdView.loadAd(adRequest);

        // インタースティシャルを作成する。
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_unit_id));
        mInterstitialAd.loadAd(adRequest);
    }

    private void startLoad() {
        TalkDao dao = new TalkDao(this);
        mTalk = dao.findByUrl(mUrl.toString());
        if (mTalk != null) {
            // DBにデータがある場合の描画処理
            TalkMetaData talkMetaData = mTalk.getTalkMetaData().iterator().next();
            mTitleView.setText(talkMetaData.getTitle());
            mUserView.setText(talkMetaData.getUser());
            setPageNumbers(1, mTalk.getSlides().size());
            mSlideAdapter.notifyDataSetChanged();
            return;
        }

        mWebView.loadUrl(mUrl.toString());
        mLoadingDialog.show(getFragmentManager(), null);
    }

    /**
     * インタースティシャルを表示する準備が出来ていたら表示する。
     */
    public void displayInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void setPageNumbers(int current, int max) {
        mPageNumbers.setText(current + "/" + max);
    }

    private void shareUrl() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mUrl);
        startActivity(intent);
    }

    private void shareBrowser() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(mUrl);
        startActivity(intent);
    }

    private void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private static class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            Log.d(TAG, "path  = " + uri.getPath());
            if (!path.startsWith("/player/")) {
                // 初回の読み込み
                view.loadUrl("javascript:srcHolder.setSrc($('.speakerdeck-iframe').attr('src'), $('#talk-details header h1').text(), $('#talk-details header h2 a').text())");
            } else {
                // playerの読み込み
                view.loadUrl("javascript:srcHolder.setTalk(JSON.stringify(talk))");
            }
        }
    }

    class SrcHolderInterface {
        @JavascriptInterface
        public void setSrc(final String src, final String title, final String user) {
            final String url = "https:" + src;
            Log.d(TAG, "src = " + src);
            mTalkMetaData = new TalkMetaData();
            mTalkMetaData.setTitle(title);
            mTalkMetaData.setUser(user);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(url);
                    mTitleView.setText(title);
                    mUserView.setText(user);
                }
            });
        }

        @JavascriptInterface
        public void setTalk(String talk) {
            Log.d(TAG, "talk = " + talk);
            try {
                Gson gson = new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create();
                mTalk = gson.fromJson(URLDecoder.decode(talk, "UTF-8"), Talk.class);
                Log.d(TAG, "talkObject = " + mTalk);
                AnalyticsManager.sendEvent(TAG, AnalyticsManager.Action.START.name(), mTalk.getUrl());

                // TODO
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TalkDao dao = new TalkDao(SlideActivity.this);
                        dao.saveIfNotExists(mTalk, mTalk.getSlides(), mTalkMetaData);
                    }
                });

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoadingDialog.dismiss();
                        setPageNumbers(1, mTalk.getSlides().size());
                        mSlideAdapter.notifyDataSetChanged();
                    }
                });
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class SlideAdapter extends PagerAdapter {
        private LayoutInflater mInflater;

        SlideAdapter(Context c) {
            super();
            mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if (mTalk == null) {
                return 0;
            }
            return mTalk.getSlides().size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view.equals(o);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Slide slide = mTalk.getSlides().get(position);
            final View layout = mInflater.inflate(R.layout.view_slide, container, false);
            final ImageView imageView = (ImageView) layout.findViewById(R.id.slide_image);

            Glide.with(SlideActivity.this)
                    .load(slide.getOriginal())
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            ViewGroup.LayoutParams params = mViewPager.getLayoutParams();

                            WindowManager wm = getWindowManager();
                            Display display = wm.getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);
                            float aspect = (float) size.x / size.y;
                            if (aspect < 1) {
                                params.width = mViewPager.getWidth();
                                params.height = (int) ((float) resource.getIntrinsicHeight() / resource.getIntrinsicWidth() * mViewPager.getWidth());
                            } else {
                                params.width = (int) ((float) resource.getIntrinsicWidth() / resource.getIntrinsicHeight() * mViewPager.getHeight());
                                params.height = mViewPager.getHeight();
                            }
                            mViewPager.setLayoutParams(params);
                            return false;
                        }
                    })
                    .into(imageView);

            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
