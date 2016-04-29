package hm.orz.chaos114.android.slideviewer;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

public class SlideViewerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        LeakCanary.install(this);
    }
}
