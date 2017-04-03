package pl.brokenpipe.timer.screens.timer

import android.databinding.BaseObservable
import android.databinding.Bindable
import pl.brokenpipe.timer.BR
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber

class TimerViewModel(val timerViewActions: TimerViewActions) : BaseObservable() {

    init {
        timerViewActions.getTimerSecondsObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                           timeInSec = it
                           time = secondsToTime(it)
                           if (it == 0L && isClockRunning) {
                               timerViewActions.playEndSound()
                               pauseTimer()
                           } else {
                               if (isClockRunning) {
                                   timerViewActions.animateTimeFlow()
                               }
                           }
                       })
    }

    @get:Bindable
    var isClockRunning = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.clockRunning)
        }

    var timeInSec = 0L

    @get:Bindable
    var time: String = secondsToTime(0)
        set(value) {
            field = value
            notifyPropertyChanged(BR.time)
        }

    fun onStartPauseClick() {
        if (isClockRunning) pauseTimer() else startTimer()
        timerViewActions.stopEndSound()
    }

    private fun pauseTimer() {
        timerViewActions.pauseTimer()
        timerViewActions.letScreenOff()
    }

    private fun startTimer() {
        if (timeInSec > 0L) {
            timerViewActions.startTimer()
            timerViewActions.keepScreenOn()
        }
    }

    private fun secondsToTime(seconds: Long): String {
        val sec = seconds % 60
        val min = (seconds - sec) / 60 % 60
        val hour = seconds.div(3600)
        if (hour > 0) {
            return "%d:%02d:%02d".format(hour, min, sec)
        } else if(min > 0){
            return "%02d:%02d".format(min, sec)
        } else {
            return "%02d".format(sec)
        }
    }

    fun subscribeClockState(stateObservable: Observable<Boolean>) {
        stateObservable.subscribe({ isClockRunning = it })
    }
}