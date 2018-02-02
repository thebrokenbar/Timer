/*
 * *
 *  * Copyright 2017 Grzegorz Wierzchanowski
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package pl.brokenpipe.timeboxing.ui.clock2

import android.content.Context
import android.content.res.TypedArray
import android.databinding.*
import android.graphics.*
import android.graphics.Bitmap.Config.ARGB_8888
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.View.OnTouchListener
import pl.brokenpipe.timeboxing.R
import pl.brokenpipe.timeboxing.extensions.HOUR_IN_MILLIS
import pl.brokenpipe.timeboxing.extensions.toClockAngle
import pl.brokenpipe.timeboxing.extensions.toClockTimeMillis
import pl.brokenpipe.timeboxing.ui.clock.AngleHelper
import timber.log.Timber
import kotlin.math.roundToInt


@BindingMethods(
        BindingMethod(type = ClockView::class, attribute = "time", method = "setTime"),
        BindingMethod(type = ClockView::class, attribute = "timeAttrChanged",
                method = "setTimeAttrChangedInverseBindingListener"),
        BindingMethod(type = ClockView::class, attribute = "onClockFaceTouchListener",
                method = "setOnClockFaceTouchListener"),
        BindingMethod(type = ClockView::class, attribute = "currentSpinSide", method = "setCurrentSpinSide"),
        BindingMethod(type = ClockView::class, attribute = "currentSpinSideAttrChanged",
                method = "setCurrentSpinSideInverseBindingListener")
)
@InverseBindingMethods(
        InverseBindingMethod(type = ClockView::class, attribute = "time", method = "getTime"),
        InverseBindingMethod(type = ClockView::class, attribute = "currentSpinSide", method = "getCurrentSpinSide")
)
class ClockView : SurfaceView, OnTouchListener {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attributeSet = attrs
        init()
    }

    private var attributeSet: AttributeSet? = null

    private val handleDragAngle = 15
    private val maxFullSpins = Int.MAX_VALUE

    var currentSpinSideInverseBindingListener: InverseBindingListener? = null
    var timeAttrChangedInverseBindingListener: InverseBindingListener? = null

    var onDragStartListener: OnClockFaceTouchListener.HandleDragStart? = null
    var onDragStopListener: OnClockFaceTouchListener.HandleDragStop? = null
    var onDraggingListener: OnClockFaceTouchListener.HandleDragging? = null

    private lateinit var attributes: ClockViewAttributes
    private lateinit var  clockPalette: ClockPalette
    private val angleHelper: AngleHelper = AngleHelper()

    private var clockRect: RectF = RectF()
    private var faceCenter: PointF = PointF(0f, 0f)
    private val faceShape: Path = Path()
    private val clockHand: Path = Path()
    private var cornerAngles: Array<Double> = emptyArray()
    private var cornerPoints: Array<PointF> = emptyArray()

    private var faceDividersBitmap = Bitmap.createBitmap(1, 1, ARGB_8888)

    ///

    private var isClockHandDragged: Boolean = false
    private var dragAngleDiff = 0.0

    var currentSpinSide: ClockSpinSide = ClockSpinSide.LEFT
        set(value) {
            field = value
            calculateCorners(clockRect)
            currentSpinSideInverseBindingListener?.onChange()
        }
    private var fullSpinsCount: Int = 0
    var angle: Double = 0.0
        set(value) {
            updateSpin(userAngleToScreenAngle(value))
            field = value
            setClockFaceShape(field)
            update()
            Timber.d("Actual angle: $field")
        }
    var time: Long
        get() {
            return (fullSpinsCount * 360 + angle).toClockTimeMillis()
        }
        set(value) {
            fullSpinsCount = (value / HOUR_IN_MILLIS).toInt()
            angle = (value % HOUR_IN_MILLIS).toClockAngle()
        }
    private var lastAngle: Double = 0.0

    ///

    private fun init() {
        val clockTypedArray = context.theme.obtainStyledAttributes(this.attributeSet,
                R.styleable.ClockView, 0, 0)
        try {
            attributes = parseAttributes(clockTypedArray)
            clockPalette = ClockPalette(attributes)
            angle = attributes.value
        } finally {
            clockTypedArray.recycle()
        }
        setOnTouchListener(this)
        setWillNotDraw(false)
    }

    private fun parseAttributes(typedArray: TypedArray): ClockViewAttributes {
        return ClockViewAttributes(
                handColor = typedArray.getColor(R.styleable.ClockView_handColor, 0xFFFFFFFF.toInt()),
                backgroundColor = typedArray.getColor(R.styleable.ClockView_backgroundColor, 0x000000),
                primaryFaceColor = typedArray.getColor(R.styleable.ClockView_primaryFaceColor, 0xffdb504a.toInt()),
                secondaryFaceColor = typedArray.getColor(R.styleable.ClockView_secondaryFaceColor, 0xffff6f59.toInt()),
                dividersColor = typedArray.getColor(R.styleable.ClockView_dividersColor, 0xFFFFFFFF.toInt()),
                dividersVisibility = typedArray.getBoolean(R.styleable.ClockView_dividersVisibility, true),
                faceVisibility = typedArray.getBoolean(R.styleable.ClockView_faceVisibility, true),
                handVisibility = typedArray.getBoolean(R.styleable.ClockView_faceVisibility, true),
                maxSpins = typedArray.getInt(R.styleable.ClockView_maxSpins, Int.MAX_VALUE),
                spinSide = ClockSpinSide.values()[(typedArray.getInt(R.styleable.ClockView_spinSide, ClockSpinSide.LEFT_RIGHT.value))],
                snapAngle = typedArray.getFloat(R.styleable.ClockView_snapAngle, 1.0f).toDouble(),
                value = typedArray.getFloat(R.styleable.ClockView_value, 0f).toDouble()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        updatePalette()

        canvas.drawRect(clockRect, clockPalette.faceBackgroundPaint)
        canvas.drawPath(faceShape, clockPalette.facePaint)
        //canvas.drawBitmap(faceDividersBitmap, 0f,0f, clockPalette.bitmapPaint)
        if (clockHand.isEmpty) {
            canvas.drawLine(clockRect.right / 2, clockRect.bottom / 2,
                    clockRect.right / 2, clockRect.top, clockPalette.handPaint)
        } else {
            canvas.drawPath(clockHand, clockPalette.handPaint)
        }
    }

    private fun updatePalette() {
        when {
            fullSpinsCount <= 0 -> {
                clockPalette.faceBackgroundPaint.color = attributes.backgroundColor
                clockPalette.facePaint.color = attributes.primaryFaceColor
            }
            fullSpinsCount % 2 != 0 -> {
                clockPalette.faceBackgroundPaint.color = attributes.primaryFaceColor
                clockPalette.facePaint.color = attributes.secondaryFaceColor
            }
            else -> {
                clockPalette.faceBackgroundPaint.color = attributes.secondaryFaceColor
                clockPalette.facePaint.color = attributes.primaryFaceColor
            }
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                handleReleaseTouch(event)
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                handleTouch(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                handleDrag(event)
                return true
            }
        }
        return false
    }

    private fun handleReleaseTouch(event: MotionEvent) {
        val angle = angleHelper.getAngle(faceCenter.x, faceCenter.y, event.x, event.y)
        if (isClockHandDragged) {
            this.angle = snapAngle(angle)
            timeAttrChangedInverseBindingListener?.onChange()
            isClockHandDragged = false
            onDragStopListener?.onHandleDragStop(Math.abs(this.angle) + 360 * fullSpinsCount)
            Timber.d("UP: $angle")
        }
    }

    private fun handleTouch(event: MotionEvent) {
        val angle = angleHelper.getAngle(faceCenter.x, faceCenter.y, event.x, event.y)
        dragAngleDiff = this.angle - angle
        isClockHandDragged = isHandleDragged(Math.abs(angle))
        if (isClockHandDragged) {
            onDragStartListener?.onHandleDragStart(Math.abs(angle) + 360 * fullSpinsCount)
        }
        Timber.d("DOWN: $angle")
    }

    private fun handleDrag(event: MotionEvent) {
        val angle = angleHelper.getAngle(faceCenter.x, faceCenter.y, event.x, event.y)
        if (isClockHandDragged) {
            this.angle = angleHelper.standarizeAngle(Math.abs(angle) + dragAngleDiff)
            timeAttrChangedInverseBindingListener?.onChange()
            onDraggingListener?.onHandleDragging(Math.abs(angle) + 360 * fullSpinsCount)
        }
        Timber.d("MOVE: $angle")
    }

    private fun snapAngle(angle: Double): Double {
        val snapAngle = attributes.snapAngle
        if (snapAngle == 0.0) return angle
        val e = (angle / snapAngle).roundToInt()
        val w = e * snapAngle
        return if (angle - w >= snapAngle / 2) w + snapAngle else w
    }

    private fun isHandleDragged(it: Double): Boolean {
        return (Math.abs(angle - it) < handleDragAngle)
                || (Math.abs(angle - it) > 360 - handleDragAngle)
    }

    private fun calculateCorners(rect: RectF) {
        val rectCenter = PointF(rect.right / 2f, rect.bottom / 2f)
        val firstCornerPoint = PointF(rect.left, rect.top)
        val secondCornerPoint = PointF(rect.left, rect.bottom)
        val thirdCornerPoint = PointF(rect.right, rect.bottom)
        val fourthCornerPoint = PointF(rect.right, rect.top)

        cornerPoints = arrayOf(firstCornerPoint, secondCornerPoint, thirdCornerPoint,
                fourthCornerPoint)

        cornerPoints.reverse()

        val firstCornerAngle = angleHelper.getAngle(rectCenter, firstCornerPoint)
        val secondCornerAngle = angleHelper.getAngle(rectCenter, secondCornerPoint)
        val thirdCornerAngle = angleHelper.getAngle(rectCenter, thirdCornerPoint)
        val fourthCornerAngle = angleHelper.getAngle(rectCenter, fourthCornerPoint)

        cornerAngles = arrayOf(firstCornerAngle,
                secondCornerAngle, thirdCornerAngle,
                fourthCornerAngle)
        cornerAngles.reverse()
    }

    private fun getIncludedCornersPoints(clockHandAngle: Double): Array<PointF> {
        var result: Array<PointF> = emptyArray()
        for (i in cornerAngles.size - 1 downTo 0) {
            if (clockHandAngle > cornerAngles[i]) {
                val rangeStart = if (isClockRightSided()) i else 0
                val rangeEnd = if (isClockRightSided()) cornerAngles.size else i + 1
                result = cornerPoints.copyOfRange(rangeStart, rangeEnd)
                if (isClockRightSided()) result.reverse()
                break
            }
        }
        if (isClockRightSided() && result.isEmpty())
            result = cornerPoints.reversedArray()
        return result
    }

    private fun isClockRightSided(): Boolean {
        return currentSpinSide == ClockSpinSide.RIGHT
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clockRect.set(0f, 0f, w.toFloat(), h.toFloat())
        faceCenter = PointF(w / 2f, h / 2f)
        calculateCorners(clockRect)
        setClockFaceShape(angle)
        update()
    }

    private fun update() {
        invalidate()
    }

    private fun setClockFaceShape(angle: Double) {
        val rectDiagonalLength = Math.sqrt(
                Math.pow(clockRect.right.toDouble(), 2.0) + Math.pow(
                        clockRect.bottom.toDouble(), 2.0)).toFloat()

        faceShape.reset()

        faceShape.moveTo(faceCenter.x, faceCenter.y)
        faceShape.lineTo(faceCenter.x, 0f)

        for (point in getIncludedCornersPoints(angle)) {
            faceShape.lineTo(point.x, point.y)
        }

        val lineEnd = getLineEnd(faceCenter.x, faceCenter.y,
                userAngleToScreenAngle(angle),
                rectDiagonalLength)
        faceShape.lineTo(lineEnd.x, lineEnd.y)

        faceShape.lineTo(faceCenter.x, faceCenter.y)

        clockHand.reset()
        clockHand.moveTo(faceCenter.x, faceCenter.y)
        clockHand.lineTo(lineEnd.x, lineEnd.y)
    }

    private fun getLineEnd(startX: Float, startY: Float, angle: Double, lineLength: Float): PointF {
        val rAngle = 360f - angle
        return PointF(
                (startX + lineLength / 2 * Math.sin(Math.toRadians(rAngle))).toFloat(),
                (startY + lineLength / 2 * Math.cos(Math.toRadians(rAngle))).toFloat()
        )
    }

    private fun updateSpin(angle: Double): Double {
        if (angle > 90 && angle < 270) {
            if (isFullSpinLeft(angle)) {
                if (currentSpinSide == ClockSpinSide.LEFT) {
                    fullSpinsCount = Math.min(fullSpinsCount + 1, maxFullSpins - 1)
                } else if (currentSpinSide == ClockSpinSide.RIGHT) {
                    fullSpinsCount--
                    if (fullSpinsCount < 0) {
                        currentSpinSide = ClockSpinSide.LEFT
                        fullSpinsCount = 0
                    }
                } else {
                    currentSpinSide = ClockSpinSide.LEFT
                }
            } else if (isFullSpinRight(angle)) {
                if (currentSpinSide == ClockSpinSide.RIGHT) {
                    fullSpinsCount = Math.min(fullSpinsCount + 1, maxFullSpins - 1)
                } else if (currentSpinSide == ClockSpinSide.LEFT) {
                    fullSpinsCount--
                    if (fullSpinsCount < 0) {
                        currentSpinSide = ClockSpinSide.RIGHT
                        fullSpinsCount = 0
                    }
                } else {
                    currentSpinSide = ClockSpinSide.RIGHT
                }
            }
        }

        lastAngle = angle

        if (fullSpinsCount < 0) {
            fullSpinsCount = -1
            lastAngle = 180.0
            return lastAngle
        }
        return angle
    }

    private fun isFullSpinRight(angle: Double): Boolean {
        val last = angleHelper.rotateAngle(lastAngle, -180.0)
        val current = angleHelper.rotateAngle(angle, -180.0)
        return last < 90 && current > 270
    }

    private fun isFullSpinLeft(angle: Double): Boolean {
        val last = angleHelper.rotateAngle(lastAngle, -180.0)
        val current = angleHelper.rotateAngle(angle, -180.0)
        return last > 270 && current < 90
    }

    private fun userAngleToScreenAngle(userAngle: Double): Double {
        return angleHelper.rotateAngle(userAngle, -180.0)
    }
}