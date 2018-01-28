package pl.brokenpipe.timeboxing.dagger.module

import dagger.Module
import dagger.Provides
import pl.brokenpipe.timeboxing.screens.timer.newtimer.Countdown
import javax.inject.Singleton

/**
 * Created by gwierzchanowski on 28.01.2018.
 */
@Module
class TimeModule {

    @Provides
    @Singleton
    fun provideCountdown(): Countdown {
        return Countdown()
    }
}