package hm.orz.chaos114.android.slideviewer.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hm.orz.chaos114.android.slideviewer.ocr.OcrRecognizer;

@Module
public class OcrModule {
    @Singleton
    @Provides
    public OcrRecognizer provideOcrUtil(Application app) {
        return new OcrRecognizer(app);
    }
}
