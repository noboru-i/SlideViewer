package hm.orz.chaos114.android.slideviewer.ocr;

import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository;
import hm.orz.chaos114.android.slideviewer.ocr.model.OcrRequest;
import hm.orz.chaos114.android.slideviewer.ocr.model.OcrResult;
import hm.orz.chaos114.android.slideviewer.ocr.util.DirectorySettings;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
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
                    baseApi.init(DirectorySettings.getTessdataDir(context).getParentFile().getAbsolutePath(), repository.getSelectedLanguage());
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
}
