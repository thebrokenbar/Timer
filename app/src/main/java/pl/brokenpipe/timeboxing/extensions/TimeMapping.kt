package pl.brokenpipe.timeboxing.extensions

import pl.brokenpipe.timeboxing.ui.clock2.ClockSpinSide

/**
 * Created by gwierzchanowski on 29.01.2018.
 */

const val SECOND_TO_MILLIS = 1000L

fun Double.toClockTimeMillis(): Long {
    var angle = this // - 180∂
//    if (angle < 0) {
//        angle = 360 - angle
//    }

    return (angle / 0.1 * SECOND_TO_MILLIS).toLong()
}

fun Long.toClockAngle(side: ClockSpinSide): Double {
    val seconds = this / SECOND_TO_MILLIS
    return seconds * 0.1
}