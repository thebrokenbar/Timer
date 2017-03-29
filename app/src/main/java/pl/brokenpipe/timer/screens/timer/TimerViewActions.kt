package pl.brokenpipe.timer.screens.timer

import rx.Observable

interface TimerViewActions {
    fun startTimer()
    fun pauseTimer()
    fun getTimerSecondsObservable(): Observable<Long>
    fun playEndSound()
    fun stopEndSound()
    fun animateTimeFlow()
    fun keepScreenOn()
    fun letScreenOff()
}