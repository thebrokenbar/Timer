package pl.brokenpipe.timer.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.View.OnTouchListener
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS


/**
 * Created by wierzchanowskig on 05.03.2017.
 */
class ClockFace(context: Context, attributeSet: AttributeSet)
    : SurfaceView(context, attributeSet), OnTouchListener {


    val facePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val clockHandAngleObservable: PublishSubject<Float> = PublishSubject.create<Float>()
    val onSizeChangeObservable: PublishSubject<RectF> = PublishSubject.create<RectF>()

    init {
        with(facePaint) {
            style = Paint.Style.FILL
            color = 0xffff0000.toInt()
        }
        setOnTouchListener(this)
        setWillNotDraw(false)
        invalidate()
        observeChanges()
    }

    private var faceCenter: PointF = PointF(0f, 0f)
    private var path: Path = Path()

    private fun observeChanges() {
        Observable.combineLatest(
            clockHandAngleObservable, onSizeChangeObservable,
            { angle, rect -> makePath(angle, rect) })
            .subscribeOn(Schedulers.computation())
            .sample(16, MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ invalidate() })
    }

    private fun makePath(angle: Float, rect: RectF): Path {
        val rectDiagonalLength = Math.sqrt(
            Math.pow(rect.right.toDouble(), 2.0) + Math.pow(
                rect.bottom.toDouble(), 2.0))
        faceCenter = PointF((rect.right - rect.left) / 2, (rect.bottom - rect.top) / 2)

        path.reset()

        path.moveTo(faceCenter.x, faceCenter.y)
        path.lineTo(faceCenter.x, 0f)


        for(point in getIncludedCornersPoints(angle, rect)) {
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
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val angle = getAngle(faceCenter, PointF(event.x, event.y))
                Timber.d("Touch angle %f", angle)
                clockHandAngleObservable.onNext(angle)
                return true
            }
        }
        return false
    }

    private fun getIncludedCornersPoints(clockHandAngle: Float, rect: RectF): Array<PointF>{
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

        if(angle > fourthCornerAngle) {
            return arrayOf(firstCornerPoint, secondCornerPoint, thirdCornerPoint, fourthCornerPoint)
        } else if(angle > thirdCornerAngle) {
            return arrayOf(firstCornerPoint, secondCornerPoint, thirdCornerPoint)
        } else if(angle > secondCornerAngle) {
            return arrayOf(firstCornerPoint, secondCornerPoint)
        } else if(angle > firstCornerAngle) {
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        onSizeChangeObservable.onNext(RectF(0f, 0f, w.toFloat(), h.toFloat()))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(path, facePaint)
    }
}