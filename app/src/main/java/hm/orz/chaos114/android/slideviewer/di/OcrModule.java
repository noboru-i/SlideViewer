package hm.orz.chaos114.android.slideviewer.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hm.orz.chaos114.android.slideviewer.ocr.OcrUtil;

@Module
public class OcrModule {
    @Singleton
    @Provides
    public OcrUtil provideOcrUtil(Application app) {
        return new OcrUtil(app);
    }
}
