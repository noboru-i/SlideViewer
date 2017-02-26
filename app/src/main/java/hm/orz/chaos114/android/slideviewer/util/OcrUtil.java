package hm.orz.chaos114.android.slideviewer.util;

import android.content.Context;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

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

    public static File getTessdataDir(Context context) {
        File dir = new File(context.getExternalFilesDir("tesseract"), "tessdata");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("cannot create file dir.");
        }
        return dir;
    }
}
