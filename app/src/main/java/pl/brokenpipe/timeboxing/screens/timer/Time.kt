package pl.brokenpipe.timeboxing.screens.timer

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 * Created by wierzchanowskig on 06.04.2017.
 */
data class Time(var hours: Long, var minutes: Long, var seconds: Long) {

    constructor(totalSeconds: Long) : this(totalSeconds / 3600,
                                           (totalSeconds % 3600) / 60,
                                           (totalSeconds % 3600) % 60)

    private val subject: PublishSubject<Time> = PublishSubject.create()

    fun incrementSecond(): Time {
        if (isZero()) return this
        val newTime = Time(hours, minutes, seconds)

        newTime.seconds++
        if (newTime.seconds >= 60) {
            newTime.seconds = 0
            newTime.minutes++
            if (newTime.minutes >= 60) {
                newTime.minutes = 0
                newTime.hours++
            }
        }
        return newTime
    }

    fun decrementSecond(): Time {
        if (isZero()) return this
        val newTime = Time(hours, minutes, seconds)

        newTime.seconds--
        if (newTime.seconds < 0) {
            newTime.seconds = 59
            newTime.minutes--
            if (newTime.minutes < 0) {
                newTime.minutes = 59
                newTime.hours--
            }
        }
        return newTime
    }

    fun hoursToString(): String {
        return "%d".format(Math.abs(hours))
    }

    fun minutesToString(): String {
        return "%02d".format(Math.abs(minutes))
    }

    fun secondsToString(): String {
        if (isZero())
            return "0"
        else
            return "%02d".format(Math.abs(seconds))
    }

    fun getTotalSeconds() = seconds + hours * 60 * 60 + minutes * 60

    fun isZero(): Boolean = seconds + hours + minutes == 0L

    @Suppress("USELESS_CAST")
    fun observe(): Observable<Time> {
        return subject.doOnNext { Timber.i("Time: ${getTotalSeconds()}") } as Observable<Time>
    }

    fun setTime(time: Time) {
        hours = time.hours
        minutes = time.minutes
        seconds = time.seconds
        subject.onNext(this)
    }
}