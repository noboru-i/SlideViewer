package hm.orz.chaos114.android.slideviewer.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import hm.orz.chaos114.android.slideviewer.pref.SettingPrefs;
import lombok.Value;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Single;
import timber.log.Timber;

/**
 * Util class for OCR.
 * use https://github.com/rmtheis/tess-two
 */
public final class OcrUtil {
    private OcrUtil() {
        // prevent initialize
    }

    public static Single<String> recognizeText(Context context, Bitmap bitmap) {
        return Single.create(subscriber -> {
            SettingPrefs settingPrefs = SettingPrefs.get(context);
            if (!settingPrefs.getEnableOcr()) {
                subscriber.onSuccess("");
                return;
            }
            Bitmap converted = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            TessBaseAPI baseApi = new TessBaseAPI();
            // TODO need to copy this
            baseApi.init(getTessdataDir(context).getParentFile().getAbsolutePath(), "eng");
            baseApi.setImage(converted);
            String recognizedText = baseApi.getUTF8Text();
            baseApi.end();
            subscriber.onSuccess(recognizedText);
        });
    }

    public static Single<File> download(Context context, Language language) {
        return Single.create(subscriber -> {
            Request request = new Request.Builder()
                    .url(language.getUrl())
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Timber.d(e, "onFailure");
                            subscriber.onError(e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Timber.d("onResponse: %d", response.code());

                            File dir = OcrUtil.getTessdataDir(context);
                            File file = new File(dir, language.getLabel() + ".traineddata");
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(file);
                                fos.write(response.body().bytes());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } finally {
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        // ignore
                                        Timber.d(e);
                                    }
                                }
                            }
                            subscriber.onSuccess(file);
                        }
                    }
            );
        });
    }

    private static File getTessdataDir(Context context) {
        File dir = new File(context.getExternalFilesDir("tesseract"), "tessdata");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("cannot create file dir.");
        }
        return dir;
    }

    @Value
    public static class Language {
        String label;
        String url;
    }
}
