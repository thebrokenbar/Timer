package pl.brokenpipe.timer.screens.timer

import android.databinding.BaseObservable
import android.databinding.Bindable
import pl.brokenpipe.timer.BR
import rx.functions.Action1
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Created by wierzchanowskig on 05.03.2017.
 */
class TimerViewModel(val timerViewActions: TimerViewActions) : BaseObservable() {

    init {
        timerViewActions.getTimerSecondsObservable()
            .subscribe({
                           time = secondsToTime(it)
                           Timber.d("time = %s", time)
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
        if (isClockRunning) timerViewActions.pauseTimer() else timerViewActions.startTimer()
        isClockRunning = !isClockRunning
    }

    private fun secondsToTime(seconds: Long): String {
        val sec = seconds % 60
        val min = (seconds - sec) / 60 % 60
        val hour = seconds.div(3600)
        return "%d:%02d:%02d".format(hour, min, sec)
    }
}
