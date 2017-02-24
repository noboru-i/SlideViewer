package hm.orz.chaos114.android.slideviewer.util;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import hm.orz.chaos114.android.slideviewer.model.Slide;
import hm.orz.chaos114.android.slideviewer.pref.SettingPrefs;
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

    // TODO needs refactor
    public static Single<String> recognizeText(SettingPrefs settingPrefs, Bitmap bitmap) {
        return Single.create(subscriber -> {
            if (!settingPrefs.getEnableOcr()) {
                subscriber.onSuccess("");
                return;
            }
            Bitmap converted = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            TessBaseAPI baseApi = new TessBaseAPI();
            // TODO need to copy this
            baseApi.init("/mnt/sdcard/tesseract", "eng");
            baseApi.setImage(converted);
            String recognizedText = baseApi.getUTF8Text();
            baseApi.end();
            Timber.d("recognizedText: %s", recognizedText);
            subscriber.onSuccess(recognizedText);
        });
    }
}
