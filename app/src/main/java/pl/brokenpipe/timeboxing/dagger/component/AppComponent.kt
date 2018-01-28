package pl.brokenpipe.timeboxing.dagger.component

import dagger.Component
import pl.brokenpipe.timeboxing.dagger.module.TimeModule
import pl.brokenpipe.timeboxing.screens.timer.newtimer.TimerController
import javax.inject.Singleton

/**
 * Created by gwierzchanowski on 28.01.2018.
 */
@Singleton
@Component(modules = [TimeModule::class])
interface AppComponent {
    fun inject(timerController: TimerController)
}