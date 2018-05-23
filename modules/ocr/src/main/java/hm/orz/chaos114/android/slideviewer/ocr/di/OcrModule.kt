package hm.orz.chaos114.android.slideviewer.ocr.di

import android.app.Application
import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.slideviewer.ocr.OcrRecognizer
import javax.inject.Singleton

@Module
class OcrModule {
    @Singleton
    @Provides
    fun provideOcrUtil(app: Application): OcrRecognizer {
        return OcrRecognizer(app)
    }
}
