package pl.brokenpipe.timeboxing.screens.timer.newtimer

import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import pl.brokenpipe.timeboxing.extensions.SECOND_IN_MILLIS
import pl.brokenpipe.timeboxing.extensions.toClockTimeMillis
import pl.brokenpipe.timeboxing.screens.timer.newtimer.exceptions.TimerDisposedException
import pl.brokenpipe.timeboxing.screens.timer.newtimer.interfaces.TimerView
import pl.brokenpipe.timeboxing.screens.timer.newtimer.interfaces.TimerViewModel
import pl.brokenpipe.timeboxing.ui.timebox.TimeboxState

/**
 * Created by wierzchanowskig@gmail.com on 22.01.2018.
 */

class TimerViewModel(
        override val view: TimerView,
        override val viewState: TimerViewState,
        private val countdown: Countdown
) : TimerViewModel {
    private val timerFlowSpeed: Long = SECOND_IN_MILLIS

    private var timerSubscriptionDisposable: Disposable? = null


    override fun startTimer() {
        viewState.running = TimeboxState.RUN
        timerSubscriptionDisposable = countdown.start(viewState.timeInMillis, timerFlowSpeed)
                .subscribeBy(
                        onNext = {
                            viewState.timeInMillis = it
                        },
                        onError = {
                            when (it) {
                                is TimerDisposedException -> {
                                    viewState.timeInMillis = 0
                                }
                                else -> view.showTimerFatalError()
                            }
                        },
                        onComplete = {
                            viewState.running = TimeboxState.FINISH
                            view.showTimerEnd()
                        }
                )
    }

    override fun pauseTimer() {
        viewState.running = TimeboxState.PAUSE
        timerSubscriptionDisposable?.dispose()
        countdown.pause()
    }

    override fun setTimeByAngle(angle: Double) {
        viewState.timeInMillis = angle.toClockTimeMillis()
    }

    override fun toggleState() {
        when {
            viewState.running == TimeboxState.RUN -> pauseTimer()
            viewState.running == TimeboxState.PAUSE -> startTimer()
            viewState.running == TimeboxState.FINISH -> if (viewState.timeInMillis > 0) startTimer()
        }
    }

    override fun setSetupState(setup: Boolean) {
        if (setup) {
            pauseTimer()
            viewState.running = TimeboxState.SET
        } else {
            if (viewState.timeInMillis > 0) {
                startTimer()
                viewState.running = TimeboxState.RUN
            }
        }
    }

}
