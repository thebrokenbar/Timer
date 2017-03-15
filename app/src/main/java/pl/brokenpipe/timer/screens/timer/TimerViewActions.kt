package pl.brokenpipe.timer.screens.timer

import rx.Observable

/**
 * Created by wierzchanowskig on 12.03.2017.
 */
interface TimerViewActions {
    fun startTimer()
    fun pauseTimer()
    fun getTimerSecondsObservable(): Observable<Long>
}