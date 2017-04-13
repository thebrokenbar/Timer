package pl.brokenpipe.timeboxing.backgroundservice

import android.os.Binder
import pl.brokenpipe.timeboxing.screens.timer.Time
import rx.Observable
import rx.subjects.BehaviorSubject
import kotlin.concurrent.timer

/**
 * Created by wierzchanowskig on 11.04.2017.
 */
class TimerServiceBinder(val timerService: TimerService): Binder() {
    fun getTime(): Time {
        return timerService.time
    }

    fun getTimerObservable(): Observable<Long> {
        return timerService.getTimerObservable()
    }

    fun getStateObservable(): BehaviorSubject<Boolean> {
        return timerService.onStateChange;
    }
}