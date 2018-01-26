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
import android.databinding.BindingMethod
import android.databinding.BindingMethods
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.View.OnTouchListener
import pl.brokenpipe.timeboxing.R
import pl.brokenpipe.timeboxing.ui.clock.AngleHelper
import pl.brokenpipe.timeboxing.ui.clock.Side
import pl.brokenpipe.timeboxing.ui.clock.Side.LEFT
import pl.brokenpipe.timeboxing.ui.clock.Side.RIGHT
import timber.log.Timber

/**
 * Created by wierzchanowskig on 04.07.2017.
 */
@BindingMethods(
        BindingMethod(type = ClockView::class, attribute = "onDragStartListener", method = "setOnDragStartListener"),
        BindingMethod(type = ClockView::class, attribute = "onDragStopListener", method = "setOnDragStopListener"),
        BindingMethod(type = ClockView::class, attribute = "onDraggingListener", method = "setOnDraggingListener")
)
class ClockView(context: Context, attributeSet: AttributeSet) :
        SurfaceView(context, attributeSet), OnTouchListener {
    private val HANDLE_DRAG_ANGLE = 15
    private val MAX_FULL_SPINS = Int.MAX_VALUE

    var onDragStartListener: OnClockFaceTouchListener.HandleDragStart? = null
    var onDragStopListener: OnClockFaceTouchListener.HandleDragStop? = null
    var onDraggingListener: OnClockFaceTouchListener.HandleDragging? = null

    private val attributes: ClockViewAttributes
    private val clockPalette: ClockPalette
    private val angleHelper: AngleHelper = AngleHelper()

    private var clockRect: RectF = RectF()
    private var faceCenter: PointF = PointF(0f, 0f)
    private val faceShape: Path = Path()
    private val clockHand: Path = Path()
    private var cornerAngles: Array<Float> = emptyArray()
    private var cornerPoints: Array<PointF> = emptyArray()

    private var faceDividersBitmap = Bitmap.createBitmap(1, 1, ARGB_8888)

    ///

    private var isClockHandDragged: Boolean = false
    private var timeInSec: Long = 0
    var currentSpinSide: Side = LEFT
        set(value) {
            field = value
            calculateCorners(clockRect)
        }
    private var fullSpinsCount: Int = 0
    private var angle: Float = 0f
    private var lastAngle: Float = 0f

    ///

    init {
        val clockTypedArray = context.theme.obtainStyledAttributes(attributeSet, R.styleable.ClockView, 0, 0)
        try {
            attributes = parseAttributes(clockTypedArray)
            clockPalette = ClockPalette(attributes)
        } finally {
            clockTypedArray.recycle()
        }
        setWillNotDraw(false)
        setOnTouchListener(this)
    }

    private fun parseAttributes(typedArray: TypedArray): ClockViewAttributes {
        return ClockViewAttributes(
                handColor = typedArray.getColor(R.styleable.ClockView_handColor, 0xFFFFFFFF.toInt()),
                backgroundColor = typedArray.getColor(R.styleable.ClockView_backgroundColor, 0xFFFFFFFF.toInt()),
                primaryFaceColor = typedArray.getColor(R.styleable.ClockView_primaryFaceColor, 0xFFdb504a.toInt()),
                secondaryFaceColor = typedArray.getColor(R.styleable.ClockView_secondaryFaceColor, 0xffff6f59.toInt()),
                dividersColor = typedArray.getColor(R.styleable.ClockView_dividersColor, 0xFFFFFFFF.toInt()),
                dividersVisibility = typedArray.getBoolean(R.styleable.ClockView_dividersVisibility, true),
                faceVisibility = typedArray.getBoolean(R.styleable.ClockView_faceVisibility, true),
                handVisibility = typedArray.getBoolean(R.styleable.ClockView_faceVisibility, true),
                maxSpins = typedArray.getInt(R.styleable.ClockView_maxSpins, Int.MAX_VALUE),
                spinSide = ClockSpinSide.values()[(typedArray.getInt(R.styleable.ClockView_spinSide, ClockSpinSide.LEFT_RIGHT.value))],
                value = 0.0f
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(clockRect, clockPalette.faceBackgroundPaint)
        canvas.drawPath(faceShape, clockPalette.facePaint)
        canvas.drawBitmap(faceDividersBitmap, 0f, 0f, clockPalette.bitmapPaint)
        if (clockHand.isEmpty) {
            canvas.drawLine(clockRect.right / 2, clockRect.bottom / 2,
                    clockRect.right / 2, clockRect.top, clockPalette.handPaint)
        } else {
            canvas.drawPath(clockHand, clockPalette.handPaint)
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                isClockHandDragged = false
                onDragStopListener?.onHandleDragStop()
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                val angle = angleHelper.getAngle(faceCenter.x, faceCenter.y, event.x, event.y)
                isClockHandDragged = isHandleDragged(Math.abs(angle))
                if (isClockHandDragged) {
                    onDragStartListener?.onHandleDragStart()
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val angle = angleHelper.getAngle(faceCenter.x, faceCenter.y, event.x, event.y)
                if (isClockHandDragged) {
                    updateAngle(Math.abs(angle))
                    onDraggingListener?.onHandleDragging(Math.abs(angle))
                }
                update()
                return true
            }
        }
        return false
    }

    private fun updateAngle(angle: Float) {
        this.lastAngle = this.angle
        this.angle = angle
        updateSpin(angle)
    }

    private fun isHandleDragged(it: Float): Boolean {
        val timerAngle = getCurrentTimeAngle()
        return (Math.abs(timerAngle - it) < HANDLE_DRAG_ANGLE)
                || (Math.abs(timerAngle - it) > 360 - HANDLE_DRAG_ANGLE)
    }

    private fun getCurrentTimeAngle(): Float {
        return angleHelper.getAngleByTimeWithValidSide(timeInSec, currentSpinSide)
    }

    private fun calculateCorners(rect: RectF) {
        val rectCenter = PointF(rect.right / 2f, rect.bottom / 2f)
        val firstCornerPoint = PointF(rect.left, rect.top)
        val secondCornerPoint = PointF(rect.left, rect.bottom)
        val thirdCornerPoint = PointF(rect.right, rect.bottom)
        val fourthCornerPoint = PointF(rect.right, rect.top)

        cornerPoints = arrayOf(firstCornerPoint, secondCornerPoint, thirdCornerPoint,
                fourthCornerPoint)

//        if (isClockRightSided()) {
//            cornerPoints.reverse()
//        }

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
            if (angleHelper.rotateAngle(clockHandAngle, -180f) > cornerAngles[i]) {
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
        return currentSpinSide == RIGHT
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clockRect.set(0f, 0f, w.toFloat(), h.toFloat())
        faceCenter = PointF(w / 2f, h / 2f)
        calculateCorners(clockRect)
        setClockFaceShape(angle)
        update()
    }

    fun update() {
        invalidate()
    }

    private fun setClockFaceShape(angle: Float) {
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
                angle,
                rectDiagonalLength)
        faceShape.lineTo(lineEnd.x, lineEnd.y)

        faceShape.lineTo(faceCenter.x, faceCenter.y)

        clockHand.reset()
        clockHand.moveTo(faceCenter.x, faceCenter.y)
        clockHand.lineTo(lineEnd.x, lineEnd.y)
    }

    private fun getLineEnd(startX: Float, startY: Float, angle: Float, lineLength: Float): PointF {
        return PointF(
                (startX + lineLength / 2 * Math.sin(Math.toRadians(angle.toDouble()))).toFloat(),
                (startY + lineLength / 2 * Math.cos(Math.toRadians(angle.toDouble()))).toFloat()
        )
    }

    private fun updateSpin(angle: Float): Float {
        Timber.d("angle: %.2f lastAngle: %.2f", angle, lastAngle)

        if (angle > 90 && angle < 270) {
            if (isFullSpinLeft(angle)) {
                if (currentSpinSide == LEFT) {
                    fullSpinsCount = Math.min(fullSpinsCount + 1, MAX_FULL_SPINS - 1)
                } else if (currentSpinSide == RIGHT) {
                    fullSpinsCount--
                    if (fullSpinsCount < 0) {
                        currentSpinSide = LEFT
                        fullSpinsCount = 0
                    }
                } else {
                    currentSpinSide = LEFT
                }
            } else if (isFullSpinRight(angle)) {
                if (currentSpinSide == RIGHT) {
                    fullSpinsCount = Math.min(fullSpinsCount + 1, MAX_FULL_SPINS - 1)
                } else if (currentSpinSide == LEFT) {
                    fullSpinsCount--
                    if (fullSpinsCount < 0) {
                        currentSpinSide = RIGHT
                        fullSpinsCount = 0
                    }
                } else {
                    currentSpinSide = RIGHT
                }
            }
        }

        lastAngle = angle
        Timber.d("full spins: %d, side: %s", fullSpinsCount, currentSpinSide.name)

        if (fullSpinsCount < 0) {
            fullSpinsCount = -1
            lastAngle = 180f
            return lastAngle
        }
        return angle
    }

    private fun isFullSpinRight(angle: Float): Boolean {
        val last = angleHelper.rotateAngle(lastAngle, -180f)
        val current = angleHelper.rotateAngle(angle, -180f)
        return last < 90 && current > 270
    }

    private fun isFullSpinLeft(angle: Float): Boolean {
        val last = angleHelper.rotateAngle(lastAngle, -180f)
        val current = angleHelper.rotateAngle(angle, -180f)
        return last > 270 && current < 90
    }
}