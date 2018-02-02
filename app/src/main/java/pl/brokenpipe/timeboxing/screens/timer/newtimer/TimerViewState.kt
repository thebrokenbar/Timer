package pl.brokenpipe.timeboxing.screens.timer.newtimer

import android.databinding.Bindable
import pl.brokenpipe.timeboxing.BR
import pl.brokenpipe.timeboxing.arch.ViewState
import pl.brokenpipe.timeboxing.ui.timebox.TimeboxState
import timber.log.Timber

/**
 * Created by wierzchanowskig@gmail.com on 22.01.2018.
 */

class TimerViewState : ViewState() {
    @get:Bindable
    var timeInMillis: Long = 0
        set(value) {
            field = value
            Timber.d("clock time: $value")
            notifyPropertyChanged(BR.timeInMillis)
        }
    @get:Bindable
    var running: TimeboxState = TimeboxState.FINISH
        set(value) {
            field = value
            notifyPropertyChanged(BR.running)
        }
}