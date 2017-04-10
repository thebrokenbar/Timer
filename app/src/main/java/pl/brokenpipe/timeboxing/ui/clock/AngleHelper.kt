package pl.brokenpipe.timeboxing.ui.clock

import android.graphics.PointF

/**
 * Created by wierzchanowskig on 23.03.2017.
 */
open class AngleHelper {
    private val ONE_SECOND_ANGLE = 0.1f

    fun getAngle(point1x: Float, point1y: Float, point2x: Float, point2y: Float ): Float {
        val target = PointF(point1x - point2x, point2y - point1y)
        val angle = Math.toDegrees(
            Math.atan2(target.y.toDouble(), target.x.toDouble())).toFloat()

        return rotateAngle(angle, -90f)
    }

    fun getAngle(point1: PointF, point2: PointF): Float {
        return getAngle(point1.x, point1.y, point2.x, point2.y)
    }

    fun rotateAngle(angle: Float, rotation: Float): Float {
        var result = angle + rotation
        if (result < 0) {
            result += 360f
        }
        return result
    }

    fun secondsToAngle(seconds: Long): Float {
        val calculatedAngle = seconds * ONE_SECOND_ANGLE
        return if(calculatedAngle >= 360)
            calculatedAngle - (calculatedAngle - (calculatedAngle % 360))
        else calculatedAngle
    }

    fun angleToSeconds(it: Float, fullSpinsCount: Int): Long {
        return (rotateAngle(it, -180f) / ONE_SECOND_ANGLE + fullSpinsCount * 3600).toLong()
    }
}