package hm.orz.chaos114.android.slideviewer.di

import android.app.Application

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import hm.orz.chaos114.android.slideviewer.ocr.OcrRecognizer

@Module
class OcrModule {
    @Singleton
    @Provides
    fun provideOcrUtil(app: Application): OcrRecognizer {
        return OcrRecognizer(app)
    }
}
