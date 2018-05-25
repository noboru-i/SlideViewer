package hm.orz.chaos114.android.slideviewer.ocr

import android.content.Context
import android.graphics.Bitmap
import hm.orz.chaos114.android.slideviewer.ocr.model.OcrResult
import io.reactivex.Observable

abstract class OcrRecognizer(context: Context) {
    abstract fun recognize(url: String, bitmap: Bitmap)

    abstract fun listen(): Observable<OcrResult>
}