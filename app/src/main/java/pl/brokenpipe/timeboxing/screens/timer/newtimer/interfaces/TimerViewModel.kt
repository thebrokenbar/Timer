package pl.brokenpipe.timeboxing.screens.timer.newtimer.interfaces

import pl.brokenpipe.timeboxing.arch.ViewModel
import pl.brokenpipe.timeboxing.screens.timer.newtimer.TimerViewState

/**
 * Created by gwierzchanowski on 26.01.2018.
 */
interface TimerViewModel : ViewModel<TimerView, TimerViewState> {
    fun startTimer()
    fun pauseTimer()
    fun setTimeByAngle(angle: Double)
    fun toggleState()
    fun setSetupState(setup: Boolean)
}