package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySettingBinding;
import hm.orz.chaos114.android.slideviewer.pref.SettingPrefs;
import hm.orz.chaos114.android.slideviewer.util.AnalyticsManager;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = SettingActivity.class.getSimpleName();

    private ActivitySettingBinding binding;

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);

        AnalyticsManager.sendScreenView(TAG);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() == null) {
            throw new AssertionError("getSupportActionBar() needs non-null.");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        init();
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


    // TODO move to ViewModel
    private void init() {
        SettingPrefs prefs = SettingPrefs.get(this);
        binding.settingSwitch.setChecked(prefs.getEnableOcr());

        binding.settingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setEnableOcr(isChecked));
        binding.selectLanguageLayout.setOnClickListener(v -> SelectOcrLanguageActivity.start(this));
    }
}
