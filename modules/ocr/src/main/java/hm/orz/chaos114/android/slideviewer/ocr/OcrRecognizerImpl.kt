package hm.orz.chaos114.android.slideviewer.ocr

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import com.googlecode.tesseract.android.TessBaseAPI
import hm.orz.chaos114.android.slideviewer.infra.repository.SettingsRepository
import hm.orz.chaos114.android.slideviewer.ocr.model.OcrRequest
import hm.orz.chaos114.android.slideviewer.ocr.model.OcrResult
import hm.orz.chaos114.android.slideviewer.ocr.util.DirectorySettings
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

/**
 * Util class for OCR.
 * use https://github.com/rmtheis/tess-two
 */
class OcrRecognizerImpl(context: Context) : OcrRecognizer(context) {
    private val subject = BehaviorSubject.create<OcrRequest>()
    private val observable: Observable<OcrResult>

    init {
        observable = subject
                .observeOn(Schedulers.computation())
                .filter { (_, _) ->
                    Log.d("OcrModule", "filter")
                    val repository = SettingsRepository(context)
                    if (!repository.enableOcr || TextUtils.isEmpty(repository.selectedLanguage)) {
                        Log.d("OcrModule", "return blank, " + repository.enableOcr + " , " + repository.selectedLanguage)
                        false
                    } else {
                        true
                    }
                }
                .map { (url, bitmap) ->
                    Log.d("OcrModule", "map")
                    val converted = bitmap.copy(Bitmap.Config.ARGB_8888, false)
                    Timber.d("start recognize: %s", url)
                    Log.d("OcrModule", "start recognize: " + url)
                    val baseApi = TessBaseAPI()
                    val repository = SettingsRepository(context)
                    baseApi.init(DirectorySettings.getTessdataDir(context).parentFile.absolutePath, repository.selectedLanguage)
                    baseApi.setImage(converted)
                    val recognizedText = baseApi.utF8Text
                    baseApi.end()
                    Timber.d("end recognize: %s", url)
                    Log.d("OcrModule", "end recognize: " + url)
                    OcrResult(url, recognizedText)
                }
    }

    override fun recognize(url: String, bitmap: Bitmap) {
        Log.d("OcrModule", "url is " + url)
        subject.onNext(OcrRequest(url, bitmap))
    }

    override fun listen(): Observable<OcrResult> {
        return observable
    }
}
