package pl.brokenpipe.timeboxing.ui.clock

import pl.brokenpipe.timeboxing.BuildConfig
import pl.brokenpipe.timeboxing.ui.clock.Side.LEFT
import pl.brokenpipe.timeboxing.ui.clock.Side.RIGHT
import pl.brokenpipe.timeboxing.ui.clock.interfaces.ClockFaceActions
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import java.io.Closeable
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

class ClockLogic(val angleHelper: AngleHelper, val clockFaceActions: ClockFaceActions) : Closeable {

    private val TIMER_FLOW_SPEED: Long = if (BuildConfig.FAST) 60 else 1
    private val HANDLE_DRAG_ANGLE = 15
    private val MAX_FULL_SPINS = Int.MAX_VALUE

    private val onAngleChangeObservable: PublishSubject<Float> = PublishSubject.create()
    private val onTimeSetSubject: PublishSubject<Long> = PublishSubject.create()
    private val onStateChange: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private val timerThread = Schedulers.newThread()

    private var fullSpinsCount = 0
    var lastAngle = 180f

    private var clockHandleAngleOffset: Float = 0f
    private var isClockHandDragged: Boolean = false
    private var isRunning = false
        set(value) {
            field = value
            onStateChange.onNext(field)
        }
    var clockSpinSide: Side = LEFT
        set(value) {
            field = value
            clockFaceActions.changeSide(field)
        }

    var timeInSec: Long = 0
        private set(value) {
            if (value >= 0) {
                emitTime(value)
                field = value
            } else {
                field = 0
                emitTime(0)
            }
        }

    //
    private var timerSetSubscription = subscribeTimerChanges(getTouchAngleObservable())
    private var timerRunSubscription = subscribeTimerChanges(getTimerAngleObservable())
    private val compositeSubscription =
        CompositeSubscription(timerSetSubscription, timerRunSubscription)

    private fun subscribeTimerChanges(observable: Observable<Float>): Subscription {
        return observable.subscribeOn(Schedulers.computation())
            .filter({ isRunning || isClockHandDragged })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                           clockFaceActions.setClockFacePaints(fullSpinsCount)
                           val angle = it
                           clockFaceActions.setClockFaceShape(angle)
                           clockFaceActions.update()
                       }, { throwable -> Timber.e(throwable) })
    }

    fun getTouchAngleObservable(): Observable<Float> {
        return onAngleChangeObservable
            .map { updateFullSpins(it) }
            .doOnNext {
                val angle = if (clockSpinSide == RIGHT) 360 - it else it
                val seconds = angleHelper.angleToSeconds(angle, fullSpinsCount)
                timeInSec = snapToMinutes(seconds)
            }
    }

    private fun snapToMinutes(seconds: Long): Long {
        return (Math.round(seconds / 60f) * 60).toLong()
    }

    private fun getTimerAngleObservable(): Observable<Float> {
        return getTimerSecondsObservable().map {
            angleHelper.getAngleByTimeWithValidSide(it, clockSpinSide)
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
        return angleHelper.getAngleByTimeWithValidSide(timeInSec, clockSpinSide)
    }

    private fun updateFullSpins(angle: Float): Float {
        Timber.d("angle: %.2f lastAngle: %.2f", angle, lastAngle)

        if (angle > 90 && angle < 270) {
            if (isFullSpinnedLeft(angle)) {
                if (clockSpinSide == LEFT) {
                    fullSpinsCount = Math.min(fullSpinsCount + 1, MAX_FULL_SPINS - 1)
                } else if (clockSpinSide == RIGHT) {
                    fullSpinsCount--
                    if (fullSpinsCount < 0) {
                        clockSpinSide = LEFT
                        fullSpinsCount = 0
                    }
                } else {
                    clockSpinSide = LEFT
                }
            } else if (isFullSpinnedRight(angle)) {
                if (clockSpinSide == RIGHT) {
                    fullSpinsCount = Math.min(fullSpinsCount + 1, MAX_FULL_SPINS - 1)
                } else if (clockSpinSide == LEFT) {
                    fullSpinsCount--
                    if (fullSpinsCount < 0) {
                        clockSpinSide = RIGHT
                        fullSpinsCount = 0
                    }
                } else {
                    clockSpinSide = RIGHT
                }
            }
        }

        lastAngle = angle
        Timber.d("full spins: %d, side: %s", fullSpinsCount, clockSpinSide.name)

        if (fullSpinsCount < 0) {
            fullSpinsCount = -1
            lastAngle = 180f
            return lastAngle
        }
        return angle
    }

    private fun isFullSpinnedRight(angle: Float): Boolean {
        val last = angleHelper.rotateAngle(lastAngle, -180f)
        val current = angleHelper.rotateAngle(angle, -180f)
        return last < 90 && current > 270
    }

    private fun isFullSpinnedLeft(angle: Float): Boolean {
        val last = angleHelper.rotateAngle(lastAngle, -180f)
        val current = angleHelper.rotateAngle(angle, -180f)
        return last > 270 && current < 90
    }

    private fun getTimerObservable() = Observable
        .interval(1000 / TIMER_FLOW_SPEED, MILLISECONDS, timerThread)
        .filter { isRunning }
        .timeInterval()

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
            val angle = angleHelper.getAngleByTimeWithValidSide(snappedTime, clockSpinSide)
            emitAngle(angle)
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
            clockHandleAngleOffset = angle - getCurrentTimeAngle()
            lastAngle = angleHelper.getAngleByTimeWithValidSide(timeInSec, clockSpinSide)
            pause()
        }
    }

    fun onTouchMove(angle: Float) {
        if (isClockHandDragged) {
            emitAngle(angleHelper.standarizeAngle(angle - clockHandleAngleOffset))
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

    override fun close() {
        compositeSubscription.clear()
    }

    fun setTime(seconds: Long) {
        timeInSec = seconds
    }

}