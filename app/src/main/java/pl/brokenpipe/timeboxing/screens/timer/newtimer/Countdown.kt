package pl.brokenpipe.timeboxing.screens.timer.newtimer

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by wierzchanowskig@gmail.com on 22.01.2018.
 */
class Countdown {
    companion object {
        const val SECOND_TO_MILLIS = 1000L
    }

    private val timerThread = Schedulers.newThread()

    private var timeInMillis: Long = 0
    private var running = false
    private var timerIntervalMillis = SECOND_TO_MILLIS
    private var intervalFlowable: Flowable<Long>? = null
    private var timerId = 0

    private fun make(startTimeInMillis: Long, timerIntervalMillis: Long): Flowable<Long> {
        timerId++
        this.timeInMillis = startTimeInMillis
        this.timerIntervalMillis = timerIntervalMillis
        return interval(timerIntervalMillis).share()
    }

    fun start(startTimeInMillis: Long, timerIntervalMillis: Long): Flowable<Long> {
        stop()
        this.running = true

        return intervalFlowable?.share() ?: make(startTimeInMillis, timerIntervalMillis)
                .also { intervalFlowable = it }.share()
    }

    private fun interval(timerIntervalMillis: Long): Flowable<Long> {
        val currentTimerId = timerId
        return Flowable.interval(timerIntervalMillis, TimeUnit.MILLISECONDS, timerThread)
                .timeInterval()
                .doOnComplete { running = false }
                .takeUntil { !shouldEmit(currentTimerId) }
                .takeWhile { shouldEmit(currentTimerId) }
                .map {
                    timeInMillis -= SECOND_TO_MILLIS
                    return@map timeInMillis
                }


    }

    private fun shouldEmit(currentTimerId: Int) =
            timeInMillis > 0 && running && currentTimerId == timerId

    fun resume() = intervalFlowable
            ?: throw TimerNotStarted("You cannot resume countdown, timer was not started yet")

    fun pause() {
        running = false
    }

    fun stop() {
        pause()
        timeInMillis = 0
        intervalFlowable = null
    }

    class TimerNotStarted(s: String) : RuntimeException(s)
}
