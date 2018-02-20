package hm.orz.chaos114.android.slideviewer.di

import dagger.Component
import hm.orz.chaos114.android.slideviewer.SlideViewerApplication
import hm.orz.chaos114.android.slideviewer.domain.di.DatabaseModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    DatabaseModule::class
])
interface AppComponent {
    fun inject(app: SlideViewerApplication)
}