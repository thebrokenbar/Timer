package pl.brokenpipe.timer.ui.clock

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.View.OnTouchListener
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func2
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS


/**
 * Created by wierzchanowskig on 05.03.2017.
 */
class ClockFace(context: Context, attributeSet: AttributeSet)
    : SurfaceView(context, attributeSet), OnTouchListener {

    private val HANDLE_DRAG_ANGLE = 3
    private val SECOND_ANGLE = 0.1f

    val backgroundColor = 0xff383d40.toInt()
    val baseFaceColor = 0xffd94e4a.toInt()
    val busyFaceColor = 0xffff6d59.toInt()

    val facePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val faceBackgroundPaint = Paint()

    private val onTouchPointObservable: PublishSubject<PointF> = PublishSubject.create()
    private val onSizeChangeObservable: BehaviorSubject<RectF> = BehaviorSubject.create()
    private val onFullSpinsCountChange: BehaviorSubject<Int> = BehaviorSubject.create()
    private val onTimerSecondChange: Observable<Long> = Observable.interval(1, SECONDS)
        .timeInterval().map { it.intervalInMilliseconds }
    private val onTimeSetSubject: PublishSubject<Long> = PublishSubject.create()

    var fullSpinsCount = 0

    init {
        with(facePaint) {
            style = FILL
            color = baseFaceColor
        }
        with(faceBackgroundPaint) {
            style = FILL
            color = backgroundColor
        }
        setOnTouchListener(this)
        setWillNotDraw(false)
        invalidate()
        timerSetObserveChanges()
        fullSpinsCount = 0
    }

    private var clockRect: Rect = Rect()
    private var faceCenter: PointF = PointF(0f, 0f)
    private var path: Path = Path()

    private var lastAngle = 180f
    private var isClockHandDragged: Boolean = false

    var isRunning = false
    var timeInSec: Long = 0
    set(value) {
        field = value
        onTimeSetSubject.onNext(value)
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

    private var timerSetSubscription: Subscription? = null
    private var timerRunSubscription: Subscription? = null


    private fun timerSetObserveChanges() {
        timerSetSubscription = subscribeTimerChanges(Observable.combineLatest(
            getTouchAngleObservable(), getSizeObservable(), onFullSpinsCountChange,
            { angle, rect, fullSpins ->
                updatePaint(fullSpins)
                makePath(angle, rect)
            }))
    }

    private fun timerRunObserveChange() {
        timerRunSubscription = subscribeTimerChanges(Observable.combineLatest(
            getTimerAngleObservable(), getSizeObservable(), onFullSpinsCountChange,
            { angle, rect, fullSpins ->
                updatePaint(fullSpins)
                makePath(angle, rect)
            }))
    }

    private fun subscribeTimerChanges(observable: Observable<Path>): Subscription {
        return observable.subscribeOn(Schedulers.computation())
            .sample(16, MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ invalidate() })
    }

    private fun getSizeObservable(): Observable<RectF>? {
        return onSizeChangeObservable.doOnNext {
            faceCenter = PointF((it.right - it.left) / 2, (it.bottom - it.top) / 2)
        }
    }

    private fun getTouchAngleObservable(): Observable<Float>? {
        return onTouchPointObservable
            .map { getAngle(faceCenter, PointF(it.x, it.y)) }
            .filter { isHandleDragged(it) }
            .doOnNext { updateFullSpins(it) }
            .doOnNext {
                timeInSec = (rotateAngle(it, -180f) / SECOND_ANGLE + fullSpinsCount * 3600).toLong()
            }
    }

    private fun getTimerAngleObservable(): Observable<Float>? {
        return getTimerSecondsObservable().map { rotateAngle(secondsToAngle(it), -180f) }
    }

    fun getOnTimeChangeObservable(): Observable<Long> {
        return onTimeSetSubject.asObservable()
    }

    fun getTimerSecondsObservable(): Observable<Long> {
        return onTimerSecondChange
            .map {
                Timber.d("TICK")
                timeInSec--
                fullSpinsCount = timeInSec.div(3600).toInt()
                timeInSec
            }
    }

    private fun isHandleDragged(it: Float): Boolean {
        if (!isClockHandDragged && !isRunning) {
            Timber.d("Angle - %f", it)
            if (Math.abs(lastAngle - it) < HANDLE_DRAG_ANGLE) {
                isClockHandDragged = true
            }
        }
        return isClockHandDragged
    }

    fun updatePaint(fullSpins: Int) {
        if (fullSpins == 0) {
            faceBackgroundPaint.color = backgroundColor
            facePaint.color = baseFaceColor
        } else if (fullSpins % 2 != 0) {
            faceBackgroundPaint.color = baseFaceColor
            facePaint.color = busyFaceColor
        } else {
            faceBackgroundPaint.color = busyFaceColor
            facePaint.color = baseFaceColor
        }
    }

    private fun updateFullSpins(angle: Float) {
        if (isFullSpinnedForward(angle)) {
            fullSpinsCount++
            onFullSpinsCountChange.onNext(fullSpinsCount)
        } else if (isFullSpinnedBackwards(angle)) {
            fullSpinsCount--
            onFullSpinsCountChange.onNext(fullSpinsCount)
        }
        lastAngle = angle
        Timber.d("fullSpins %d", fullSpinsCount)
    }

    private fun isFullSpinnedBackwards(angle: Float) =
        rotateAngle(lastAngle, -180f) < 90 && rotateAngle(angle, -180f) > 270

    private fun isFullSpinnedForward(angle: Float) =
        rotateAngle(lastAngle, -180f) > 270 && rotateAngle(angle, -180f) < 90

    private fun makePath(angle: Float, rect: RectF): Path {
        val rectDiagonalLength = Math.sqrt(
            Math.pow(rect.right.toDouble(), 2.0) + Math.pow(
                rect.bottom.toDouble(), 2.0))

        path.reset()

        path.moveTo(faceCenter.x, faceCenter.y)
        path.lineTo(faceCenter.x, 0f)

        for (point in getIncludedCornersPoints(angle, rect)) {
            path.lineTo(point.x, point.y)
        }

        path.lineTo(
            (faceCenter.x + rectDiagonalLength / 2 * Math.sin(Math.toRadians(
                angle.toDouble()))).toFloat(),
            (faceCenter.y + rectDiagonalLength / 2 * Math.cos(Math.toRadians(
                angle.toDouble()))).toFloat())

        path.lineTo(faceCenter.x, faceCenter.y)

        return path
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                isClockHandDragged = false
                return true
            }
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                onTouchPointObservable.onNext(PointF(event.x, event.y))
                return true
            }
        }
        return false
    }

    private fun getIncludedCornersPoints(clockHandAngle: Float, rect: RectF): Array<PointF> {
        val rectCenter = PointF(rect.right / 2f, rect.bottom / 2f)
        val firstCornerPoint = PointF(rect.left, rect.top)
        val secondCornerPoint = PointF(rect.left, rect.bottom)
        val thirdCornerPoint = PointF(rect.right, rect.bottom)
        val fourthCornerPoint = PointF(rect.right, rect.top)

        val firstCornerAngle = rotateAngle(getAngle(rectCenter, firstCornerPoint), -180f)
        val secondCornerAngle = rotateAngle(getAngle(rectCenter, secondCornerPoint), -180f)
        val thirdCornerAngle = rotateAngle(getAngle(rectCenter, thirdCornerPoint), -180f)
        val fourthCornerAngle = rotateAngle(getAngle(rectCenter, fourthCornerPoint), -180f)

        val angle = rotateAngle(clockHandAngle, -180f)

        if (angle > fourthCornerAngle) {
            return arrayOf(firstCornerPoint, secondCornerPoint, thirdCornerPoint, fourthCornerPoint)
        } else if (angle > thirdCornerAngle) {
            return arrayOf(firstCornerPoint, secondCornerPoint, thirdCornerPoint)
        } else if (angle > secondCornerAngle) {
            return arrayOf(firstCornerPoint, secondCornerPoint)
        } else if (angle > firstCornerAngle) {
            return arrayOf(firstCornerPoint)
        } else {
            return emptyArray()
        }
    }

    private fun getAngle(point1: PointF, point2: PointF): Float {
        val target = PointF(point1.x - point2.x, point2.y - point1.y)
        val angle = Math.toDegrees(
            Math.atan2((target.y - y).toDouble(), (target.x - x).toDouble())).toFloat()

        return rotateAngle(angle, -90f)
    }

    private fun rotateAngle(angle: Float, rotation: Float): Float {
        var result = angle + rotation
        if (result < 0) {
            result += 360f
        }
        return result
    }

    private fun secondsToAngle(seconds: Long): Float {
        return seconds * SECOND_ANGLE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        onSizeChangeObservable.onNext(RectF(0f, 0f, w.toFloat(), h.toFloat()))
        clockRect.set(0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(clockRect, faceBackgroundPaint)
        canvas.drawPath(path, facePaint)
    }
}