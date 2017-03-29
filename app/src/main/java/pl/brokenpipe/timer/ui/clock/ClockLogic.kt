package pl.brokenpipe.timer.ui.clock

import android.graphics.RectF
import pl.brokenpipe.timer.ui.clock.interfaces.ClockFaceActions
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

class ClockLogic(val angleHelper: AngleHelper, val clockFaceActions: ClockFaceActions) {

    private val HANDLE_DRAG_ANGLE = 15
    private val MAX_FULL_SPINS = Int.MAX_VALUE

    private val onAngleChangeObservable: PublishSubject<Float> = PublishSubject.create()
    private val onTimeSetSubject: PublishSubject<Long> = PublishSubject.create()

    private var fullSpinsCount = 0
    var lastAngle = 180f

    private var clockHandleAngleOffset: Float = 0f
    private var isClockHandDragged: Boolean = false
    private var isRunning = false
    private var timeInSec: Long = 0
        set(value) {
            if (value >= 0) {
                field = value
                emitTime(value)
            } else {
                field = 0
                emitTime(0)
                timerRunSubscription?.unsubscribe()
            }
        }

    private var timerSetSubscription: Subscription? = null
    private var timerRunSubscription: Subscription? = null

    private fun timerSetObserveChanges() {
        timerSetSubscription = subscribeTimerChanges(
            getTouchAngleObservable())
    }

    private fun timerRunObserveChange() {
        timerRunSubscription = subscribeTimerChanges(
            getTimerAngleObservable())
    }

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

    private fun getTimerSecondsObservable(): Observable<Long> {
        return getTimerObservable()
            .map {
                timeInSec--
                fullSpinsCount = timeInSec.div(3600).toInt()
                timeInSec
            }
    }

    private fun isHandleDragged(it: Float): Boolean {
        return !isRunning && Math.abs(lastAngle - it) < HANDLE_DRAG_ANGLE
    }

    private fun updateFullSpins(angle: Float):Float {
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

    private fun getTimerObservable() = Observable.interval(1, SECONDS)
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
            val angle = angleHelper.secondsToAngle(snapToMinutes(timeInSec))
            emitAngle(angleHelper.rotateAngle(angle, -180f))
        }
        isClockHandDragged = false
        if (fullSpinsCount < 0) {
            fullSpinsCount = 0
        }
    }

    fun onTouchDown(angle: Float) {
        isClockHandDragged = isHandleDragged(angle)
        if (isClockHandDragged) {
            clockHandleAngleOffset = angle - lastAngle
        }
    }

    fun onTouchMove(angle: Float) {
        if (isClockHandDragged) {
            emitAngle(angle - clockHandleAngleOffset)
        }
    }

    fun start() {
        timerSetSubscription?.unsubscribe()
        timerRunObserveChange()
        isRunning = true
    }

    fun pause() {
        timerRunSubscription?.unsubscribe()
        timerSetObserveChanges()
        isRunning = false
    }

}