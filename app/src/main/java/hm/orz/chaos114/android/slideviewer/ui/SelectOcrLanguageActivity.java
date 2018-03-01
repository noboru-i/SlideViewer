package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySelectOcrLanguageBinding;
import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository;
import hm.orz.chaos114.android.slideviewer.ocr.LanguageDownloader;
import hm.orz.chaos114.android.slideviewer.ocr.OcrUtil;
import hm.orz.chaos114.android.slideviewer.ocr.model.Language;
import hm.orz.chaos114.android.slideviewer.ocr.util.DirectorySettings;
import hm.orz.chaos114.android.slideviewer.widget.RowSelectOcrLanguageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class SelectOcrLanguageActivity extends AppCompatActivity {

    private ActivitySelectOcrLanguageBinding binding;
    private BaseAdapter adapter;

    private RowSelectOcrLanguageView.RowSelectOcrLanguageViewListener listener = new RowSelectOcrLanguageView.RowSelectOcrLanguageViewListener() {
        @Override
        public void onChangeState(RowSelectOcrLanguageView view, Language language, boolean isChecked) {
            Timber.d("onChangeState: %b", isChecked);
            if (!isChecked) {
                updatePrefs(null);
                return;
            }

            if (DirectorySettings.hasFile(SelectOcrLanguageActivity.this, language)) {
                updatePrefs(language);
                return;
            }

            view.showLoading(true);
            LanguageDownloader downloader = new LanguageDownloader();
            downloader.download(SelectOcrLanguageActivity.this, language)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> {
                        Toast.makeText(SelectOcrLanguageActivity.this, "download succeeded.", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        updatePrefs(language);
                        view.showLoading(false);
                    });
        }

        private void updatePrefs(@Nullable Language language) {
            SettingsRepository settingsRepository = new SettingsRepository(SelectOcrLanguageActivity.this);
            settingsRepository.setSelectedLanguage(language != null ? language.getId() : null);
            adapter.notifyDataSetChanged();
        }
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, SelectOcrLanguageActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_ocr_language);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() == null) {
            throw new AssertionError("getSupportActionBar() needs non-null.");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        adapter = new Adapter();
        binding.languageList.setAdapter(adapter);
    }

    private class Adapter extends BaseAdapter {

        private List<Language> rowDataList;

        private Adapter() {
            rowDataList = new ArrayList<>();
            rowDataList.add(new Language("eng", "English", "https://github.com/tesseract-ocr/tessdata/blob/3.04.00/eng.traineddata?raw=true"));
            rowDataList.add(new Language("jpn", "Japanese", "https://github.com/tesseract-ocr/tessdata/blob/3.04.00/jpn.traineddata?raw=true"));
        }

        @Override
        public int getCount() {
            return rowDataList.size();
        }

        @Override
        public Language getItem(int position) {
            return rowDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RowSelectOcrLanguageView view;
            if (convertView == null) {
                view = new RowSelectOcrLanguageView(SelectOcrLanguageActivity.this);
            } else {
                view = (RowSelectOcrLanguageView) convertView;
            }

            Language rowData = getItem(position);
            view.setData(rowData);
            view.setListener(listener);

            return view;
        }
    }
}
