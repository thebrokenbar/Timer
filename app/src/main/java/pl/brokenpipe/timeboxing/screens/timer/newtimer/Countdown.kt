package pl.brokenpipe.timeboxing.screens.timer.newtimer

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * Created by wierzchanowskig@gmail.com on 22.01.2018.
 */
class Countdown {
    companion object {
        const val SECOND_TO_MILLIS = 1000L
    }

    private val timerThread = Schedulers.newThread()

    private var timerSubject: PublishSubject<Long>? = null
    private var timeDisposable: Disposable? = null
    private var timeInMillis: Long = 0

    fun start(startTimeInMillis: Long, timerIntervalMillis: Long): Flowable<Long> {
        stop()
        timeInMillis = startTimeInMillis
        timerSubject = PublishSubject.create<Long>()
        timeDisposable = subscribeTimeCountdown(timerIntervalMillis)
        return timerSubjectToFlowable()
    }

    private fun subscribeTimeCountdown(timerIntervalMillis: Long): Disposable {
        return Flowable.interval(timerIntervalMillis, TimeUnit.MILLISECONDS, timerThread)
                .timeInterval()
                .filter { timerSubject != null }
                .filter { timeInMillis > 0 }
                .map { timerSubject as PublishSubject<Long> }
                .subscribeBy(onNext = { emitNewTime(it) })
    }

    fun observe() = timerSubjectToFlowable()

    fun pause() {
        timeDisposable?.dispose()
    }

    fun stop() {
        pause()
        timeInMillis = 0
    }

    private fun emitNewTime(it: PublishSubject<Long>) {
        timeInMillis -= SECOND_TO_MILLIS
        it.onNext(timeInMillis)
        if (timeInMillis <= 0) {
            it.onComplete()
            stop()
        }
    }

    private fun timerSubjectToFlowable() = timerSubject?.toFlowable(BackpressureStrategy.LATEST)
            ?: throw NullPointerException("timerSubject is null, perhaps you're facing thread issue")
}