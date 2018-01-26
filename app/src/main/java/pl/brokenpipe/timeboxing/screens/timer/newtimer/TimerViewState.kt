package pl.brokenpipe.timeboxing.screens.timer.newtimer

import android.databinding.BaseObservable
import android.databinding.Bindable
import pl.brokenpipe.timeboxing.BR
import pl.brokenpipe.timeboxing.arch.ViewState

/**
 * Created by wierzchanowskig@gmail.com on 22.01.2018.
 */

class TimerViewState : ViewState() {
    @get:Bindable
    var timeInMillis: Long = 0
        set(value) {
            field = value
            notifyPropertyChanged(BR.timeInMillis)
        }
    @get:Bindable
    var running: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.running)
        }
}