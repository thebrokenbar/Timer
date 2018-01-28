package pl.brokenpipe.timeboxing.extensions

import pl.brokenpipe.timeboxing.ui.clock2.ClockSpinSide
import kotlin.math.roundToInt

/**
 * Created by gwierzchanowski on 29.01.2018.
 */

const val SECOND_TO_MILLIS = 1000L

fun Double.toClockTimeMillis(): Long {
    val absAngle = if(this > 0) this - 180 else this + 180

    return (absAngle / 0.1 * SECOND_TO_MILLIS).toLong()
}

fun Long.toClockAngle(side: ClockSpinSide): Double {
    val seconds = this / SECOND_TO_MILLIS
    return seconds * 0.1 * if (side == ClockSpinSide.RIGHT) -1 else 1
}