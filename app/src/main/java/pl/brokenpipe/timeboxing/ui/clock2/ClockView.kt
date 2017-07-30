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

/**
 * Created by wierzchanowskig on 04.07.2017.
 */
class ClockView(context: Context, attributeSet: AttributeSet):
    SurfaceView(context, attributeSet), OnTouchListener {
    private val HANDLE_DRAG_ANGLE = 15

    var onClockFaceTouchListener: OnClockFaceTouchListener? = null

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

    ///

    init {
        val clockTypedArray = context.theme.obtainStyledAttributes(attributeSet, R.styleable.ClockView, 0, 0)
        try {
            attributes = parseAttributes(clockTypedArray)
            clockPalette = ClockPalette(attributes)
        } finally {
            clockTypedArray.recycle()
        }
    }

    private fun parseAttributes(typedArray: TypedArray): ClockViewAttributes {
        return ClockViewAttributes(
            handColor = typedArray.getColor(R.attr.handColor, 0xFFFFFF),
            backgroundColor = typedArray.getColor(R.attr.backgroundColor, 0x000000),
            primaryFaceColor = typedArray.getColor(R.attr.primaryFaceColor, 0xdb504a),
            secondaryFaceColor = typedArray.getColor(R.attr.secondaryFaceColor, 0xff6f59),
            dividersColor = typedArray.getColor(R.attr.dividersColor, 0xFFFFFF),
            dividersVisibility = typedArray.getBoolean(R.attr.dividersVisibility, true),
            faceVisibility = typedArray.getBoolean(R.attr.faceVisibility, true),
            handVisibility = typedArray.getBoolean(R.attr.faceVisibility, true),
            maxSpins = typedArray.getInt(R.attr.maxSpins, Int.MAX_VALUE),
            spinSide = ClockSpinSide.values()[(typedArray.getInt(R.attr.spinSide, ClockSpinSide.LEFT_RIGHT.value))],
            value = 0.0f
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawRect(clockRect, clockPalette.faceBackgroundPaint)
        canvas.drawPath(faceShape, clockPalette.facePaint)
        canvas.drawBitmap(faceDividersBitmap, 0f,0f, clockPalette.bitmapPaint)
        if(clockHand.isEmpty) {
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
                onClockFaceTouchListener?.onHandleDragStop()
                return true
            }
            MotionEvent.ACTION_DOWN -> {
                val angle = angleHelper.getAngle(faceCenter.x, faceCenter.y, event.x, event.y)
                isClockHandDragged = isHandleDragged(Math.abs(angle))
                if(isClockHandDragged) {
                    onClockFaceTouchListener?.onHandleDragStart()
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val angle = angleHelper.getAngle(faceCenter.x, faceCenter.y, event.x, event.y)
                if(isClockHandDragged) {
                    onClockFaceTouchListener?.onHandleDragging(Math.abs(angle))
                }
                return true
            }
        }
        return false
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
            if (angleHelper.rotateAngle(clockHandAngle, -180f) > cornerAngles[i] ) {
                val rangeStart = if(isClockRightSided()) i else 0
                val rangeEnd = if(isClockRightSided()) cornerAngles.size else i + 1
                result = cornerPoints.copyOfRange(rangeStart, rangeEnd)
                if(isClockRightSided()) result.reverse()
                break
            }
        }
        if(isClockRightSided() && result.isEmpty())
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
                                 angle ,
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
}