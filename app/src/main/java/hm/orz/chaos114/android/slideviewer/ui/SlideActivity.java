package hm.orz.chaos114.android.slideviewer.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.model.Slide;
import hm.orz.chaos114.android.slideviewer.model.Talk;
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager;

public class SlideActivity extends Activity {
    private static final String TAG = SlideActivity.class.getSimpleName();

    private Handler mHandler;

    private Menu mMainMenu;
    private WebView mWebView;
    private TextView mTitleView;
    private TextView mByView;
    private TextView mUserView;
    private ViewPager mViewPager;
    private TextView mPageNumbers;
    private AdView mAdView;
    private SlideAdapter mSlideAdapter;
    private LoadingDialogFragment mLoadingDialog;
    private InterstitialAd mInterstitialAd;

    private String mUrl;
    private Talk mTalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsManager.initializeAnalyticsTracker(getApplication());
        setContentView(R.layout.activity_slide);

        AnalyticsManager.sendScreenView(TAG);

        mHandler = new Handler();

        mWebView = (WebView) findViewById(R.id.slide_web_view);
        mTitleView = (TextView) findViewById(R.id.slide_title);
        mByView = (TextView) findViewById(R.id.slide_by);
        mUserView = (TextView) findViewById(R.id.slide_user);
        mViewPager = (ViewPager) findViewById(R.id.slide_view_pager);
        mPageNumbers = (TextView) findViewById(R.id.slide_page_numbers);
        mAdView = (AdView) findViewById(R.id.slide_ad_view);
        mSlideAdapter = new SlideAdapter(this);
        mLoadingDialog = LoadingDialogFragment.newInstance();

        mViewPager.setAdapter(mSlideAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
            mUrl = uri.toString();
        } else {
            mUrl = "https://speakerdeck.com/speakerdeck/introduction-to-speakerdeck";
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("6B74A80630FD70AC2DC27C79CE02AEC9").build();
        mAdView.loadAd(adRequest);

        // インタースティシャルを作成する。
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.admob_unit_id));
        mInterstitialAd.loadAd(adRequest);
    }

    private void startLoad() {
        mWebView.loadUrl(mUrl);
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
        intent.setData(Uri.parse(mUrl));
        startActivity(intent);
    }

    class MyWebViewClient extends WebViewClient {
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
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(url);
                    mTitleView.setText(title);
                    mByView.setVisibility(View.VISIBLE);
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
                AnalyticsManager.sendEvent(TAG, AnalyticsManager.Action.START.name(), mTalk.getUrl());
                Log.d(TAG, "talkObject = " + mTalk);
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
        RequestQueue mQueue;
        ImageLoader.ImageCache mImageCache;

        SlideAdapter(Context c) {
            super();
            mQueue = Volley.newRequestQueue(SlideActivity.this.getApplicationContext());
            mImageCache = new NoCacheSample();
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
            Log.d(TAG, "position = " + position);
            Slide slide = mTalk.getSlides().get(position);
            FrameLayout layout = (FrameLayout) mInflater.inflate(R.layout.slide, null);
            NetworkImageView imageView = (NetworkImageView) layout.findViewById(R.id.slide_image);
            imageView.setImageUrl(slide.getOriginal(), new ImageLoader(mQueue, mImageCache));
            container.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    class NoCacheSample implements ImageLoader.ImageCache {

        @Override
        public Bitmap getBitmap(String url) {
            return null;
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
        }
    }
}
