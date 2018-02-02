package pl.brokenpipe.timeboxing.extensions

/**
 * Created by gwierzchanowski on 29.01.2018.
 */

const val SECOND_IN_MILLIS = 1000L
const val HOUR_IN_MILLIS = SECOND_IN_MILLIS * 60 * 60

fun Double.toClockTimeMillis(): Long {
    return (this / 0.1 * SECOND_IN_MILLIS).toLong()
}

fun Long.toClockAngle(): Double {
    return this / SECOND_IN_MILLIS * 0.1
}