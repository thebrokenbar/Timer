package pl.brokenpipe.timeboxing.screens.timer.newtimer

import io.reactivex.disposables.Disposable
import pl.brokenpipe.timeboxing.BuildConfig
import pl.brokenpipe.timeboxing.arch.ViewModel

/**
 * Created by wierzchanowskig@gmail.com on 22.01.2018.
 */
class TimerViewModel(
        timerController: TimerView,
        timerViewState: TimerViewState,
        private val timeFlow: TimeFlow
) : ViewModel<TimerView, TimerViewState>(timerController, timerViewState) {

    private val timerFlowSpeed: Long =
            if (BuildConfig.DEBUG)
                timeFlow.secondInMillis / 10
            else
                timeFlow.secondInMillis

    private var running: Boolean = false
    private var timerSubscriptionDisposable: Disposable? = null

    fun startTimer() {
        running = true
        timerSubscriptionDisposable = timeFlow.run(timerFlowSpeed)
                .subscribe({
                    viewState.timeInMillis += it
                }, {
                    view.showTimerFatalError()
                })
    }

    fun stopTimer() {
        running = false
        timerSubscriptionDisposable?.dispose()
        timeFlow.stop()
    }


}