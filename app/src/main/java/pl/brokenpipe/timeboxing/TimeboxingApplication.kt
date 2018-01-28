package pl.brokenpipe.timeboxing

import android.app.Application
import pl.brokenpipe.timeboxing.dagger.component.AppComponent
import pl.brokenpipe.timeboxing.dagger.component.DaggerAppComponent
import pl.brokenpipe.timeboxing.dagger.module.TimeModule

/**
 * Created by gwierzchanowski on 28.01.2018.
 */
class TimeboxingApplication: Application() {

    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()

        component = DaggerAppComponent.builder()
                .timeModule(TimeModule())
                .build()
    }
}