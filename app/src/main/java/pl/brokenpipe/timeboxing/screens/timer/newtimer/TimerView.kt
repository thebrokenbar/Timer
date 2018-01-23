package pl.brokenpipe.timeboxing.screens.timer.newtimer

import pl.brokenpipe.timeboxing.arch.View

/**
 * Created by wierzchanowskig@gmail.com on 23.01.2018.
 */
interface TimerView: View {
    fun showTimerFatalError()
    fun showTimerEnd()
}