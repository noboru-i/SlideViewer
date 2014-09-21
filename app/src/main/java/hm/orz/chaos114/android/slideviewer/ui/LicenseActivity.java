package hm.orz.chaos114.android.slideviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hm.orz.chaos114.android.slideviewer.R;

public class LicenseActivity extends Activity {

    @InjectView(R.id.license_web_view)
    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        ButterKnife.inject(this);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle("License");
        actionBar.setDisplayHomeAsUpEnabled(true);

        mWebView.loadUrl("file:///android_asset/license.html");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
