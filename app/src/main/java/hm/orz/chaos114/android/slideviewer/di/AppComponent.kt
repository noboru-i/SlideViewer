package hm.orz.chaos114.android.slideviewer.di

import android.app.Application

import javax.inject.Singleton

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import hm.orz.chaos114.android.slideviewer.SlideViewerApplication

@Singleton
@Component(modules = arrayOf(AndroidInjectionModule::class, OcrModule::class, InfraModule::class, AppModule::class, ActivityModule::class))
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: SlideViewerApplication)
}
