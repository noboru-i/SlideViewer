package hm.orz.chaos114.android.slideviewer.ocr;

import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import lombok.Value;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Util class for OCR.
 * use https://github.com/rmtheis/tess-two
 */
public final class OcrUtil {
    private final BehaviorSubject<OcrRequest> subject = BehaviorSubject.create();
    private final Observable<OcrResult> observable;

    public OcrUtil(Context context) {
        observable = subject
                .observeOn(Schedulers.computation())
                .flatMap(ocrRequest -> Observable.create((ObservableOnSubscribe<OcrResult>) emitter -> {
                    SettingsRepository repository = new SettingsRepository(context);
                    if (!repository.getEnableOcr()
                            || repository.getSelectedLanguage() == null) {
                        emitter.onNext(new OcrResult(ocrRequest.getUrl(), ""));
                        return;
                    }
                    Bitmap converted = ocrRequest.getBitmap().copy(Bitmap.Config.ARGB_8888, false);
                    Timber.d("start recognize: %s", ocrRequest.getUrl());
                    TessBaseAPI baseApi = new TessBaseAPI();
                    baseApi.init(getTessdataDir(context).getParentFile().getAbsolutePath(), repository.getSelectedLanguage());
                    baseApi.setImage(converted);
                    String recognizedText = baseApi.getUTF8Text();
                    baseApi.end();
                    Timber.d("end recognize: %s", ocrRequest.getUrl());
                    emitter.onNext(new OcrResult(ocrRequest.getUrl(), recognizedText));
                }).subscribeOn(Schedulers.computation()));
    }

    public void recognize(String url, Bitmap bitmap) {
        subject.onNext(new OcrRequest(url, bitmap));
    }

    public Observable<OcrResult> listen() {
        return observable;
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
                            File file = new File(dir, language.getId() + ".traineddata");
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

    public static boolean hasFile(Context context, Language language) {
        File dir = OcrUtil.getTessdataDir(context);
        File file = new File(dir, language.getId() + ".traineddata");
        return file.exists();
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
        String id;
        String label;
        String url;
    }

    @Value
    public static class OcrRequest {
        String url;
        Bitmap bitmap;
    }

    @Value
    public static class OcrResult {
        String url;
        String recognizedText;
    }
}
