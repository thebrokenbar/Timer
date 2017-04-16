package pl.brokenpipe.timeboxing.ui.clock

import android.graphics.PointF

open class AngleHelper {
    private val ONE_SECOND_ANGLE = 0.1f

    fun getAngle(point1x: Float, point1y: Float, point2x: Float, point2y: Float): Float {
        val target = PointF(point1x - point2x, point2y - point1y)
        val angle = Math.toDegrees(
            Math.atan2(target.y.toDouble(), target.x.toDouble())).toFloat()

        return rotateAngle(angle, -90f)
    }

    fun getAngle(point1: PointF, point2: PointF): Float {
        return getAngle(point1.x, point1.y, point2.x, point2.y)
    }

    fun rotateAngle(angle: Float, rotation: Float): Float {
        return standarizeAngle(angle + rotation)
    }

    fun secondsToAngle(seconds: Long): Float {
        return standarizeAngle(rotateAngle(seconds * ONE_SECOND_ANGLE, 180f))
    }

    fun angleToSeconds(it: Float, fullSpinsCount: Int): Long {
        return (rotateAngle(it, -180f) / ONE_SECOND_ANGLE + fullSpinsCount * 3600).toLong()
    }

    fun standarizeAngle(angle: Float): Float {
        return if (angle >= 360) angle - (angle - (angle % 360))
        else if (angle < 0) angle + 360f
        else angle
    }
}