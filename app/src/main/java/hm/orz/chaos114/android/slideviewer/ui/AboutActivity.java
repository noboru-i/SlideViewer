package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import hm.orz.chaos114.android.slideviewer.BuildConfig;
import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.ActivityAboutBinding;
import hm.orz.chaos114.android.slideviewer.util.IntentUtil;

public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    public static void start(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about);

        initToolbar();
        initActions();
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

    private void initToolbar() {
        setSupportActionBar(binding.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayShowTitleEnabled(false);
            bar.setHomeButtonEnabled(true);
        }
    }

    private void initActions() {
        binding.aboutVersion.setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));
        binding.aboutGitHub.setOnClickListener(v ->
                IntentUtil.browse(this, "https://github.com/noboru-i/SlideViewer")
        );
        binding.aboutOtherApp.setOnClickListener(v ->
                IntentUtil.browse(this, "https://play.google.com/store/apps/developer?id=noboru")
        );
        binding.aboutLicense.setOnClickListener(v ->
                LicenseActivity.start(this)
        );
    }
}
