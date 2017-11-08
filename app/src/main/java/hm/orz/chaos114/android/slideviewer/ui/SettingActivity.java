package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySettingBinding;
import hm.orz.chaos114.android.slideviewer.pref.SettingPrefs;

public class SettingActivity extends AppCompatActivity {
    private ActivitySettingBinding binding;

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting);

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

        binding.settingSwitch.setOnClickListener(v -> {
            SwitchCompat view = (SwitchCompat) v;
            boolean isChecked = view.isChecked();
            if (isChecked && TextUtils.isEmpty(prefs.getSelectedLanguage())) {
                view.setChecked(false);
                Toast.makeText(this, R.string.setting_error_before_download, Toast.LENGTH_LONG).show();
                return;
            }
            prefs.setEnableOcr(isChecked);
        });
        binding.selectLanguageLayout.setOnClickListener(v -> SelectOcrLanguageActivity.start(this));
    }
}
