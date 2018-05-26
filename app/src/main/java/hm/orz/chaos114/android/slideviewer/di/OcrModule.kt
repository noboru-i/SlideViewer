package hm.orz.chaos114.android.slideviewer.di

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.slideviewer.ocr.OcrRecognizer
import hm.orz.chaos114.android.slideviewer.ocr.model.OcrResult
import io.reactivex.Observable
import java.lang.Exception
import javax.inject.Singleton

@Module
class OcrModule {
    @Singleton
    @Provides
    fun provideOcrUtil(app: Application): OcrRecognizer {
        try {
            val clazz = Class.forName("hm.orz.chaos114.android.slideviewer.ocr.OcrRecognizerImpl")
            val constructor = clazz.getConstructor(Context::class.java)
            return constructor.newInstance(app) as OcrRecognizer
        } catch (e: Exception) {
            Log.d("OcrModule", "OcrModule cannot load.", e)
            // return blank
            return object : OcrRecognizer(app) {
                override fun recognize(url: String, bitmap: Bitmap) {
                    // no-op
                }

                override fun listen(): Observable<OcrResult> {
                    return Observable.empty()
                }
            }
        }
    }
}
