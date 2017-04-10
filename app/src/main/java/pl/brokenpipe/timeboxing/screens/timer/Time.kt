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
        if(seconds + hours + minutes == 0L)
            return "0"
        else
            return "%02d".format(seconds)
    }
}