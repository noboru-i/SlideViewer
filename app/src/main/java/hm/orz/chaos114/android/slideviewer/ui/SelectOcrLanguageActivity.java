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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;

import hm.orz.chaos114.android.slideviewer.R;
import hm.orz.chaos114.android.slideviewer.databinding.ActivitySelectOcrLanguageBinding;
import hm.orz.chaos114.android.slideviewer.util.OcrUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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

        private List<RowData> rowDataList;
        private OkHttpClient client = new OkHttpClient();

        private Adapter() {
            rowDataList = new ArrayList<>();
            rowDataList.add(new RowData("eng", "https://github.com/tesseract-ocr/tessdata/blob/3.04.00/eng.traineddata?raw=true"));
            rowDataList.add(new RowData("jpn", "https://github.com/tesseract-ocr/tessdata/blob/3.04.00/jpn.traineddata?raw=true"));
        }

        @Override
        public int getCount() {
            return rowDataList.size();
        }

        @Override
        public RowData getItem(int position) {
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

            RowData rowData = getItem(position);
            TextView labelView = (TextView) view.findViewById(R.id.label);
            labelView.setText(rowData.label);
            view.setOnClickListener(v -> {
                        Timber.d("clicked: %s", rowData.label);
                        load(rowData);
                    }

            );
            return view;
        }

        private void load(RowData rowData) {
            Request request = new Request.Builder()
                    .url(rowData.url)
                    .build();

            client.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Timber.d(e, "onFailure");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Timber.d("onResponse: %d", response.code());
                            File dir = OcrUtil.getTessdataDir(SelectOcrLanguageActivity.this);
                            File file = new File(dir, rowData.label + ".traineddata");
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(file);
                                fos.getChannel().transferFrom(Channels.newChannel(response.body().byteStream()), 0, Long.MAX_VALUE);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } finally {
                                if (fos != null) {
                                    fos.close();
                                }
                            }
                        }
                    }
            );

        }
    }

    private static class RowData {
        String label;
        String url;

        private RowData(String label, String url) {
            this.label = label;
            this.url = url;
        }
    }
}
