package pl.brokenpipe.timeboxing.screens.timer.newtimer

import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import pl.brokenpipe.timeboxing.BuildConfig
import pl.brokenpipe.timeboxing.screens.timer.newtimer.exceptions.TimerDisposedException
import pl.brokenpipe.timeboxing.screens.timer.newtimer.interfaces.TimerView
import pl.brokenpipe.timeboxing.screens.timer.newtimer.interfaces.TimerViewModel
import kotlin.math.roundToInt

/**
 * Created by wierzchanowskig@gmail.com on 22.01.2018.
 */
class TimerViewModel(
        override val view: TimerView,
        override val viewState: TimerViewState,
        private val countdown: Countdown
) : TimerViewModel {
    private val timerFlowSpeed: Long =
            if (BuildConfig.DEBUG)
                Countdown.SECOND_TO_MILLIS
            else
                Countdown.SECOND_TO_MILLIS

    private var timerSubscriptionDisposable: Disposable? = null


    override fun startTimer() {
        viewState.running = true
        timerSubscriptionDisposable = countdown.start(viewState.timeInMillis, timerFlowSpeed)
                .subscribeBy (
                        onNext = {
                            viewState.timeInMillis = it
                        },
                        onError = {
                            when(it){
                                is TimerDisposedException -> {
                                    viewState.timeInMillis = 0
                                }
                                else -> view.showTimerFatalError()
                            }
                        },
                        onComplete = {
                            view.showTimerEnd()
                        }
                )
    }

    override fun pauseTimer() {
        viewState.running = false
        timerSubscriptionDisposable?.dispose()
        countdown.pause()
    }

    override fun setTimeByAngle(angle: Float) {
        val absAngle = kotlin.math.abs(angle)

        val fullHours = kotlin.math.floor(absAngle / 360).toInt()
        val minutes = (((absAngle % 360) / 6)).roundToInt()

        viewState.timeInMillis = (fullHours * 60 + minutes) * Countdown.SECOND_TO_MILLIS
    }


}
