package pl.brokenpipe.timeboxing.ui.clock

import android.graphics.PointF
import pl.brokenpipe.timeboxing.ui.clock.Side.RIGHT

open class AngleHelper {
    private val ONE_SECOND_ANGLE = 0.1

    fun getAngle(point1x: Float, point1y: Float, point2x: Float, point2y: Float): Double {
        val target = PointF(point1x - point2x, point2y - point1y)
        val angle = Math.toDegrees(
            Math.atan2(target.y.toDouble(), target.x.toDouble()))

        return 360 - rotateAngle(angle, 90.0)
    }

    fun getAngle(point1: PointF, point2: PointF): Double {
        return getAngle(point1.x, point1.y, point2.x, point2.y)
    }

    fun rotateAngle(angle: Double, rotation: Double): Double {
        return standarizeAngle(angle + rotation)
    }

    fun secondsToAngle(seconds: Long): Double {
        return standarizeAngle(rotateAngle(seconds * ONE_SECOND_ANGLE, 180.0))
    }

    fun angleToSeconds(it: Double, fullSpinsCount: Int): Long {
        return (rotateAngle(it, -180.0) / ONE_SECOND_ANGLE + fullSpinsCount * 3600).toLong()
    }

    fun standarizeAngle(angle: Double): Double {
        return when {
            angle >= 360 -> angle - (angle - (angle % 360))
            angle < 0 -> angle + 360f
            else -> angle
        }
    }

    fun getAngleByTimeWithValidSide(timeInSec: Long, clockSide: Side): Double {
        return if(clockSide == RIGHT) 360 - secondsToAngle(timeInSec)
        else secondsToAngle(timeInSec)
    }
}