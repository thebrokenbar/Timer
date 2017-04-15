package pl.brokenpipe.timeboxing.ui.clock

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff.Mode
import android.graphics.PorterDuff.Mode.DST_IN
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader.TileMode.CLAMP
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.View.OnTouchListener
import pl.brokenpipe.timeboxing.ui.clock.interfaces.ClockFaceActions
import rx.Observable

class ClockFace(context: Context, attributeSet: AttributeSet)
    : SurfaceView(context, attributeSet), OnTouchListener, ClockFaceActions {

    val angleHelper = AngleHelper()
    val logic = ClockLogic(angleHelper, this)

    val handColor = 0xffffffff.toInt()
    val backgroundColor = 0xff393e41.toInt()
    val baseFaceColor = 0xffdb504a.toInt()
    val busyFaceColor = 0xffff6f59.toInt()

    val facePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val handPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val faceBackgroundPaint = Paint()
    val faceDividersPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val faceDividersMaskPaint = Paint()
    val bitmapPaint = Paint()

    private var clockRect: RectF = RectF()
    private var faceCenter: PointF = PointF(0f, 0f)
    private val faceShape: Path = Path()
    private val clockHand: Path = Path()
    private var cornerAngles: Array<Float> = emptyArray()
    private var cornerPoints: Array<PointF> = emptyArray()
    private var faceDividersBitmap = Bitmap.createBitmap(1, 1, ARGB_8888)

    init {
        with(facePaint) {
            style = FILL
            color = baseFaceColor
        }
        with(faceBackgroundPaint) {
            style = FILL
            color = backgroundColor
        }
        with(handPaint) {
            style = STROKE
            color = handColor
            strokeWidth = 2f
        }
        with(faceDividersPaint) {
            style = STROKE
            color = handColor
            strokeWidth = 1.5f
        }
        with(faceDividersMaskPaint) {
            style = FILL
            color = 0xffffffff.toInt()
            xfermode = PorterDuffXfermode(DST_IN)
        }

        setOnTouchListener(this)
        setWillNotDraw(false)

        update()
        pause()
    }

    fun start() {
        logic.start()
    }

    fun pause() {
        logic.pause()
    }

    fun getStateObservable(): Observable<Boolean> = logic.getStateObservable()

    override fun update() {
        invalidate()
    }

    private fun getLineEnd(startX: Float, startY: Float, angle: Float, lineLength: Float): PointF {
        return PointF(
            (startX + lineLength / 2 * Math.sin(Math.toRadians(angle.toDouble()))).toFloat(),
            (startY + lineLength / 2 * Math.cos(Math.toRadians(angle.toDouble()))).toFloat()
        )
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                logic.onTouchUp()
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                val angle = angleHelper.getAngle(faceCenter.x, faceCenter.y, event.x, event.y)
                logic.onTouchDown(angle)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val angle = angleHelper.getAngle(faceCenter.x, faceCenter.y, event.x, event.y)
                logic.onTouchMove(angle)
                return true
            }
        }
        return false
    }

    private fun calculateCorners(rect: RectF) {
        val rectCenter = PointF(rect.right / 2f, rect.bottom / 2f)
        val firstCornerPoint = PointF(rect.left, rect.top)
        val secondCornerPoint = PointF(rect.left, rect.bottom)
        val thirdCornerPoint = PointF(rect.right, rect.bottom)
        val fourthCornerPoint = PointF(rect.right, rect.top)

        cornerPoints = arrayOf(firstCornerPoint, secondCornerPoint, thirdCornerPoint,
                               fourthCornerPoint)

        val firstCornerAngle = angleHelper.rotateAngle(
            angleHelper.getAngle(rectCenter, firstCornerPoint), -180f)
        val secondCornerAngle = angleHelper.rotateAngle(
            angleHelper.getAngle(rectCenter, secondCornerPoint), -180f)
        val thirdCornerAngle = angleHelper.rotateAngle(
            angleHelper.getAngle(rectCenter, thirdCornerPoint), -180f)
        val fourthCornerAngle = angleHelper.rotateAngle(
            angleHelper.getAngle(rectCenter, fourthCornerPoint), -180f)

        cornerAngles = arrayOf(firstCornerAngle, secondCornerAngle, thirdCornerAngle,
                               fourthCornerAngle)
    }

    private fun getIncludedCornersPoints(clockHandAngle: Float): Array<PointF> {
        var result: Array<PointF> = emptyArray()
        for (i in cornerAngles.size - 1 downTo 0) {
            if (angleHelper.rotateAngle(clockHandAngle, -180f) > cornerAngles[i] ) {
                result = cornerPoints.copyOfRange(0, i + 1)
                break
            }
        }
        return result
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clockRect.set(0f, 0f, w.toFloat(), h.toFloat())
        faceCenter = PointF(w / 2f, h / 2f)
        calculateCorners(clockRect)
        setClockFaceShape(angleHelper.rotateAngle(angleHelper.secondsToAngle(logic.timeInSec), -180f))
        update()
    }

    private fun setFaceDividers(clockRect: RectF): Bitmap {
        faceDividersBitmap.recycle()
        faceDividersBitmap = Bitmap.createBitmap(
            clockRect.right.toInt(), clockRect.bottom.toInt(), ARGB_8888)
        val canvas = Canvas(faceDividersBitmap)
        val rectCenter = PointF(clockRect.right / 2f, clockRect.bottom / 2f)
        val maskRadius = Math.min(clockRect.bottom, clockRect.right)
        val rectDiagonalLength = Math.sqrt(
            Math.pow(clockRect.right.toDouble(), 2.0) + Math.pow(
                clockRect.bottom.toDouble(), 2.0)).toFloat()

        faceDividersMaskPaint.shader = RadialGradient(
            rectCenter.x, rectCenter.y, maskRadius, intArrayOf(0x00ffffff, 0x00ffffff, 0x99ffffff.toInt()),
            floatArrayOf(0f, 0.3f, 1f), CLAMP)

        (0..11)
            .map { getLineEnd(rectCenter.x, rectCenter.y, it * 30f, rectDiagonalLength) }
            .forEach { canvas.drawLine(rectCenter.x, rectCenter.y, it.x, it.y, faceDividersPaint) }

        canvas.drawRect(clockRect, faceDividersMaskPaint)
        return faceDividersBitmap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(clockRect, faceBackgroundPaint)
        canvas.drawPath(faceShape, facePaint)
        canvas.drawBitmap(faceDividersBitmap, 0f,0f, bitmapPaint)
        if(clockHand.isEmpty) {
            canvas.drawLine(clockRect.right / 2, clockRect.bottom / 2, clockRect.right / 2, clockRect.top, handPaint)
        } else {
            canvas.drawPath(clockHand, handPaint)
        }
    }

    override fun setClockFaceShape(angle: Float) {
        val rectDiagonalLength = Math.sqrt(
            Math.pow(clockRect.right.toDouble(), 2.0) + Math.pow(
                clockRect.bottom.toDouble(), 2.0)).toFloat()

        faceShape.reset()

        faceShape.moveTo(faceCenter.x, faceCenter.y)
        faceShape.lineTo(faceCenter.x, 0f)

        for (point in getIncludedCornersPoints(angle)) {
            faceShape.lineTo(point.x, point.y)
        }

        val lineEnd = getLineEnd(faceCenter.x, faceCenter.y, angle, rectDiagonalLength)
        faceShape.lineTo(lineEnd.x, lineEnd.y)

        faceShape.lineTo(faceCenter.x, faceCenter.y)

        clockHand.reset()
        clockHand.moveTo(faceCenter.x, faceCenter.y)
        clockHand.lineTo(lineEnd.x, lineEnd.y)
    }

    override fun setClockFacePaints(fullSpinsCount: Int) {
        if (fullSpinsCount <= 0) {
            faceBackgroundPaint.color = backgroundColor
            facePaint.color = baseFaceColor
        } else if (fullSpinsCount % 2 != 0) {
            faceBackgroundPaint.color = baseFaceColor
            facePaint.color = busyFaceColor
        } else {
            faceBackgroundPaint.color = busyFaceColor
            facePaint.color = baseFaceColor
        }
    }

    fun getTimerObservable(): Observable<Long> {
        return logic.getOnTimeChangeObservable()
    }
}