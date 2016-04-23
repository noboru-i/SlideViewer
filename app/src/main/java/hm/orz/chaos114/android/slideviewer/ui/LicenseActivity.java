package hm.orz.chaos114.android.slideviewer.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import butterknife.Bind;
import butterknife.ButterKnife;
import hm.orz.chaos114.android.slideviewer.R;

public class LicenseActivity extends Activity {

    @Bind(R.id.license_web_view)
    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        ButterKnife.bind(this);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.about_license));
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
