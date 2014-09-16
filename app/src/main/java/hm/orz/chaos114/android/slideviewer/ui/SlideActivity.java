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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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

    private WebView mWebView;
    private ViewPager mViewPager;
    private AdView mAdView;
    private SlideAdapter mSlideAdapter;
    private LoadingDialogFragment mLoadingDialog;

    private Talk mTalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsManager.initializeAnalyticsTracker(getApplication());
        setContentView(R.layout.activity_slide);

        AnalyticsManager.sendScreenView(TAG);

        mHandler = new Handler();

        mWebView = (WebView) findViewById(R.id.slide_web_view);
        mViewPager = (ViewPager) findViewById(R.id.slide_view_pager);
        mAdView = (AdView) findViewById(R.id.slide_ad_view);
        mSlideAdapter = new SlideAdapter(this);
        mLoadingDialog = LoadingDialogFragment.newInstance();

        mViewPager.setAdapter(mSlideAdapter);

        String url;
        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            Log.d(TAG, "uri = " + uri.toString());
            url = uri.toString();
        } else {
            url = "https://speakerdeck.com/sys1yagi/eclipsedeandroidapurikesiyonkai-fa-gaxu-sarerufalsehaxiao-xue-sheng-madedayone";
        }
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.addJavascriptInterface(new SrcHolderInterface(), "srcHolder");
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.loadUrl(url);

        mLoadingDialog.show(getFragmentManager(), null);

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

    class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            Uri uri = Uri.parse(url);
            String path = uri.getPath();
            Log.d(TAG, "path  = " + uri.getPath());
            if (!path.startsWith("/player/")) {
                // 初回の読み込み
                view.loadUrl("javascript:srcHolder.setSrc($('.speakerdeck-iframe').attr('src'))");
            } else {
                // playerの読み込み
                view.loadUrl("javascript:srcHolder.setTalk(JSON.stringify(talk))");
            }
        }
    }

    class SrcHolderInterface {
        @JavascriptInterface
        public void setSrc(String src) {
            final String url = "https:" + src;
            Log.d(TAG, "src = " + src);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(url);
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
            AnalyticsManager.sendEvent(TAG, AnalyticsManager.Action.CHANGE_PAGE.name(), Integer.toString(position));
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
