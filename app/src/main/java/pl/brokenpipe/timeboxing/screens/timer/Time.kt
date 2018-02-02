package pl.brokenpipe.timeboxing.screens.timer

/**
 * Created by wierzchanowskig on 06.04.2017.
 */
data class Time(var hours: Long, var minutes: Long, var seconds: Long) {

    fun hoursToString(): String {
        return "%d".format(hours)
    }

    fun minutesToString(): String {
        return "%02d".format(minutes)
    }

    fun secondsToString(): String {
        return if(isZero())
            "0"
        else
            "%02d".format(seconds)
    }

    fun roundedMinutesToString(): String {
        return "%02d".format(minutes + (seconds/30).toInt())
    }

    fun getTotalSeconds() = seconds + hours * 60 * 60 + minutes * 60

    fun isZero(): Boolean = seconds + hours + minutes == 0L
}