package pl.brokenpipe.timeboxing.ui.clock

import pl.brokenpipe.timeboxing.BuildConfig
import pl.brokenpipe.timeboxing.ui.clock.interfaces.ClockFaceActions
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS

class ClockLogic(val angleHelper: AngleHelper, val clockFaceActions: ClockFaceActions) {

    private val TIMER_FLOW_SPEED: Long = if(BuildConfig.FAST) 60 else 1
    private val HANDLE_DRAG_ANGLE = 15
    private val MAX_FULL_SPINS = Int.MAX_VALUE

    private val onAngleChangeObservable: PublishSubject<Float> = PublishSubject.create()
    private val onTimeSetSubject: PublishSubject<Long> = PublishSubject.create()
    private val onStateChange: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private var fullSpinsCount = 0
    var lastAngle = 180f

    private var clockHandleAngleOffset: Float = 0f
    private var isClockHandDragged: Boolean = false
    private var isRunning = false
        set(value) {
            field = value
            onStateChange.onNext(field)
        }

    var timeInSec: Long = 0
        private set(value) {
            if (value >= 0) {
                field = value
                emitTime(value)
            } else {
                field = 0
                emitTime(0)
            }
        }

    private var timerSetSubscription = subscribeTimerChanges(getTouchAngleObservable())
    private var timerRunSubscription = subscribeTimerChanges(getTimerAngleObservable())

    private fun subscribeTimerChanges(observable: Observable<Float>): Subscription {
        return observable.subscribeOn(Schedulers.computation())
            .sample(8, MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ angle ->
                           clockFaceActions.setClockFacePaints(fullSpinsCount)
                           clockFaceActions.setClockFaceShape(angle)
                           clockFaceActions.update()
                       }, { throwable -> Timber.e(throwable) })
    }

    fun getTouchAngleObservable(): Observable<Float> {
        return onAngleChangeObservable
            .map { updateFullSpins(it) }
            .doOnNext {
                val seconds = angleHelper.angleToSeconds(it, fullSpinsCount)
                timeInSec = snapToMinutes(seconds)
            }
    }

    private fun snapToMinutes(seconds: Long): Long {
        return (Math.round(seconds / 60f) * 60).toLong()
    }

    private fun getTimerAngleObservable(): Observable<Float> {
        return getTimerSecondsObservable().map {
            angleHelper.rotateAngle(angleHelper.secondsToAngle(it), -180f)
        }
    }

    fun getOnTimeChangeObservable(): Observable<Long> {
        return onTimeSetSubject.asObservable()
    }

    fun getStateObservable(): Observable<Boolean> {
        return onStateChange
    }

    private fun getTimerSecondsObservable(): Observable<Long> {
        return getTimerObservable()
            .map {
                timeInSec--
                fullSpinsCount = timeInSec.div(3600).toInt()
                timeInSec
            }
    }

    private fun isHandleDragged(it: Float): Boolean {
        val timerAngle = getCurrentTimeAngle()
        return (Math.abs(timerAngle - it) < HANDLE_DRAG_ANGLE)
            || (Math.abs(timerAngle - it) > 360 - HANDLE_DRAG_ANGLE)
    }

    private fun getCurrentTimeAngle(): Float {
        var timerAngle = angleHelper.secondsToAngle(timeInSec)
        timerAngle = angleHelper.rotateAngle(timerAngle, -180f)
        return timerAngle
    }

    private fun updateFullSpins(angle: Float): Float {
        if (isFullSpinnedForward(angle)) {
            fullSpinsCount = Math.min(fullSpinsCount + 1, MAX_FULL_SPINS - 1)
        } else if (isFullSpinnedBackwards(angle)) {
            fullSpinsCount--
        }

        lastAngle = angle
        Timber.d("full spins: %d", fullSpinsCount)

        if (fullSpinsCount < 0) {
            fullSpinsCount = -1
            lastAngle = 180f
            return lastAngle
        }
        return angle
    }

    private fun isFullSpinnedBackwards(angle: Float) =
        angleHelper.rotateAngle(lastAngle, -180f) < 90
            && angleHelper.rotateAngle(angle, -180f) > 270

    private fun isFullSpinnedForward(angle: Float) =
        angleHelper.rotateAngle(lastAngle, -180f) > 270
            && angleHelper.rotateAngle(angle, -180f) < 90

    private fun getTimerObservable() = Observable.interval(1000 / TIMER_FLOW_SPEED, MILLISECONDS)
        .filter { isRunning }
        .timeInterval().map { it.intervalInMilliseconds }


    ///

    fun emitAngle(angle: Float) {
        onAngleChangeObservable.onNext(angle)
    }

    fun emitTime(timeInSec: Long) {
        onTimeSetSubject.onNext(timeInSec)
    }

    fun onTouchUp() {
        if (isClockHandDragged) {
            val snappedTime = snapToMinutes(timeInSec)
            val angle = angleHelper.secondsToAngle(snappedTime)
            emitAngle(angleHelper.rotateAngle(angle, -180f))
            isClockHandDragged = false
            if (fullSpinsCount < 0) {
                fullSpinsCount = 0
            }

            if (!isRunning && snappedTime > 0) {
                start()
            }
        }
    }

    fun onTouchDown(angle: Float) {
        isClockHandDragged = isHandleDragged(angle)
        if (isClockHandDragged) {
            if (!isRunning) {
                clockHandleAngleOffset = angle - getCurrentTimeAngle()

            } else {
                pause()
                onTouchDown(angle)
            }
        }
    }

    fun onTouchMove(angle: Float) {
        if (isClockHandDragged) {
            emitAngle(angle - clockHandleAngleOffset)
        }
    }

    fun start() {
        if (timerRunSubscription.isUnsubscribed) {
            timerRunSubscription = subscribeTimerChanges(getTimerAngleObservable())
        }
        isRunning = true
    }

    fun pause() {
        if (!timerRunSubscription.isUnsubscribed) {
            timerRunSubscription.unsubscribe()
        }
        isRunning = false
    }

}