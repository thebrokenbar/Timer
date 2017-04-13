package pl.brokenpipe.timeboxing.backgroundservice

import android.app.Service
import android.content.Intent
import android.os.IBinder
import pl.brokenpipe.timeboxing.BuildConfig
import pl.brokenpipe.timeboxing.screens.timer.Time
import rx.Observable
import rx.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit.MILLISECONDS

class TimerService : Service() {
    private val TIMER_FLOW_SPEED: Long = if(BuildConfig.FAST) 60 else 1

    val time: Time = Time(0, 0, 0)
    val onStateChange: BehaviorSubject<Boolean> = BehaviorSubject.create()


    private var isRunning = false
        set(value) {
            field = value
            onStateChange.onNext(field)
        }

    fun getTimerObservable(): Observable<Long> = Observable.interval(1000 / TIMER_FLOW_SPEED, MILLISECONDS)
        .filter { isRunning }
        .timeInterval().map { it.intervalInMilliseconds }


    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }
}
