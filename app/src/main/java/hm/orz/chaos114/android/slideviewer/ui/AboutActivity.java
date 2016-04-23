package hm.orz.chaos114.android.slideviewer.ui;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import hm.orz.chaos114.android.slideviewer.BuildConfig;
import hm.orz.chaos114.android.slideviewer.R;

public class AboutActivity extends AppCompatActivity {

    @Bind(R.id.about_version)
    TextView mVersionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getString(R.string.menu_about));
        actionBar.setDisplayHomeAsUpEnabled(true);

        mVersionTextView.setText("version: " + BuildConfig.VERSION_NAME);
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

    @OnClick(R.id.about_git_hub)
    void viewGitHub() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/noboru-i/SlideViewer"));
        startActivity(intent);
    }

    @OnClick(R.id.about_other_app)
    void viewOtherApp() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id=noboru"));
        startActivity(intent);
    }

    @OnClick(R.id.about_license)
    void viewLicense() {
        Intent intent = new Intent(this, LicenseActivity.class);
        startActivity(intent);
    }
}
