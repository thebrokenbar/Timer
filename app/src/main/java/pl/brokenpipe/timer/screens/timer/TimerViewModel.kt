package pl.brokenpipe.timer.screens.timer

import android.databinding.BaseObservable
import android.databinding.Bindable
import pl.brokenpipe.timer.BR
import timber.log.Timber

/**
 * Created by wierzchanowskig on 05.03.2017.
 */
class TimerViewModel(val timerViewActions: TimerViewActions) : BaseObservable() {

    init {
        timerViewActions.getTimerSecondsObservable()
            .subscribe({
                           time = secondsToTime(it)
                           if (it == 0L && isClockRunning) {
                               timerViewActions.playEndSound()
                           }
                       })
    }

    @get:Bindable
    var isClockRunning = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.clockRunning)
        }

    @get:Bindable
    var time: String = "0:00:00"
        set(value) {
            field = value
            notifyPropertyChanged(BR.time)
        }

    fun onStartPauseClick() {
        if (isClockRunning) pauseTimer() else startTimer()
        timerViewActions.stopEndSound()
    }

    private fun pauseTimer() {
        isClockRunning = false
        timerViewActions.pauseTimer()
    }

    private fun startTimer() {
        isClockRunning = true
        timerViewActions.startTimer()
    }

    private fun secondsToTime(seconds: Long): String {
        val sec = seconds % 60
        val min = (seconds - sec) / 60 % 60
        val hour = seconds.div(3600)
        return "%d:%02d:%02d".format(hour, min, sec)
    }
}