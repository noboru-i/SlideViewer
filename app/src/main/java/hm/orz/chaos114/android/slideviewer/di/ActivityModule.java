package hm.orz.chaos114.android.slideviewer.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import hm.orz.chaos114.android.slideviewer.ui.SlideActivity;
import hm.orz.chaos114.android.slideviewer.ui.SlideListActivity;

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    public abstract SlideActivity contributeSlideActivityInjector();

    @ContributesAndroidInjector
    public abstract SlideListActivity contributeSlideListActivityInjector();
}
