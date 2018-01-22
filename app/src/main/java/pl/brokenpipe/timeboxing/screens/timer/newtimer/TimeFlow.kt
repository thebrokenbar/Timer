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
class TimeFlow {
    val secondInMillis: Long = 1000
    private val timerThread = Schedulers.newThread()
    private val timerSubject = PublishSubject.create<Long>()

    private var timeDisposable: Disposable? = null

    fun run(timerIntervalMillis: Long): Flowable<Long> {
        timeDisposable?.dispose()
        timeDisposable = Flowable.interval(timerIntervalMillis, TimeUnit.MILLISECONDS, timerThread)
                .timeInterval()
                .subscribeBy(
                        onNext = {
                            timerSubject.onNext(secondInMillis)
                        }
                )

        return timerSubject.toFlowable(BackpressureStrategy.LATEST)
    }

    fun stop() {
        timeDisposable?.dispose()
    }
}