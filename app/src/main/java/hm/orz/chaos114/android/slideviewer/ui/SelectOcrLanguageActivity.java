package hm.orz.chaos114.android.slideviewer.ui;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySelectOcrLanguageBinding;
import hm.orz.chaos114.android.slideviewer.util.OcrUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SelectOcrLanguageActivity extends AppCompatActivity {

    private ActivitySelectOcrLanguageBinding binding;

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

        binding.languageList.setAdapter(new Adapter());
    }

    private class Adapter extends BaseAdapter {

        private List<OcrUtil.Language> rowDataList;

        private Adapter() {
            rowDataList = new ArrayList<>();
            rowDataList.add(new OcrUtil.Language("eng", "https://github.com/tesseract-ocr/tessdata/blob/3.04.00/eng.traineddata?raw=true"));
            rowDataList.add(new OcrUtil.Language("jpn", "https://github.com/tesseract-ocr/tessdata/blob/3.04.00/jpn.traineddata?raw=true"));
        }

        @Override
        public int getCount() {
            return rowDataList.size();
        }

        @Override
        public OcrUtil.Language getItem(int position) {
            return rowDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_select_ocr_language, parent, false);
            } else {
                view = convertView;
            }

            OcrUtil.Language rowData = getItem(position);
            TextView labelView = (TextView) view.findViewById(R.id.label);
            labelView.setText(rowData.getLabel());
            view.setOnClickListener(v -> {
                        Timber.d("clicked: %s", rowData.getLabel());
                        OcrUtil.download(parent.getContext(), rowData)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(file -> {
                                    Toast.makeText(SelectOcrLanguageActivity.this, "download succeeded.", Toast.LENGTH_SHORT).show();
                                });
                    }

            );
            return view;
        }
    }
}
