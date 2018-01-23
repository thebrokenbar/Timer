package pl.brokenpipe.timeboxing.screens.timer.newtimer

import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import pl.brokenpipe.timeboxing.BuildConfig
import pl.brokenpipe.timeboxing.arch.ViewModel

/**
 * Created by wierzchanowskig@gmail.com on 22.01.2018.
 */
class TimerViewModel(
        timerController: TimerView,
        timerViewState: TimerViewState,
        private val countdown: Countdown
) : ViewModel<TimerView, TimerViewState>(timerController, timerViewState) {

    private val timerFlowSpeed: Long =
            if (BuildConfig.DEBUG)
                Countdown.SECOND_TO_MILLIS / 10
            else
                Countdown.SECOND_TO_MILLIS

    private var running: Boolean = false
    private var timerSubscriptionDisposable: Disposable? = null

    fun startTimer() {
        running = true
        timerSubscriptionDisposable = countdown.start(viewState.timeInMillis, timerFlowSpeed)
                .subscribeBy (
                        onNext = {
                            viewState.timeInMillis = it
                        },
                        onError = {
                            view.showTimerFatalError()
                        },
                        onComplete = {
                            view.showTimerEnd()
                        }
                )
    }

    fun pauseTimer() {
        running = false
        timerSubscriptionDisposable?.dispose()
        countdown.pause()
    }


}