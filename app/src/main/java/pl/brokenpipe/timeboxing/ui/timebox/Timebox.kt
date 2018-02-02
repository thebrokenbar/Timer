package pl.brokenpipe.timeboxing.ui.timebox

import android.content.Context
import android.databinding.BindingMethod
import android.databinding.BindingMethods
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.center_timebox.view.*
import pl.brokenpipe.timeboxing.R
import pl.brokenpipe.timeboxing.extensions.SECOND_IN_MILLIS
import pl.brokenpipe.timeboxing.screens.timer.Time

@BindingMethods(
        BindingMethod(type = Timebox::class, attribute = "time", method = "setTimeInMillis"),
        BindingMethod(type = Timebox::class, attribute = "running", method = "setRunning")
)
class Timebox : RelativeLayout {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val timeDividerChar = ":"

    private val time: Time = Time(0, 0, 0)

    var running: TimeboxState = TimeboxState.PAUSE
        set(value) {
            field = value
            onStateChange()
        }

    var timeInMillis: Long = 0
        set(value) {
            field = value
            val allSeconds = field / SECOND_IN_MILLIS
            val sec = allSeconds % 60
            val min = (allSeconds - sec) / 60 % 60
            val hour = allSeconds.div(3600)
            time.hours = hour
            time.minutes = min
            time.seconds = sec
            onTimeChange()
        }

    private val timeLeft: String
        get() = if (time.hours > 0) {
            time.hoursToString()
        } else {
            if (running == TimeboxState.SET) time.roundedMinutesToString() else time.minutesToString()
        }

    private val timeRight: String
        get() = if (time.hours > 0) {
            time.minutesToString()
        } else {
            if (running == TimeboxState.SET)
                if (time.minutes > 0) "00" else "0"
            else time.secondsToString()
        }

    val text: String
        get() = "$timeLeft $timeDividerChar $timeRight"

    init {
        inflate(context, R.layout.center_timebox, this)
        tvClockTimeMiddle.text = timeDividerChar
    }

    private fun onTimeChange() {
        if (time.minutes + time.hours > 0) {
            tvClockTimeLeft.visibility = View.VISIBLE
            tvClockTimeLeft.text = timeLeft
        } else {
            tvClockTimeLeft.visibility = View.GONE
        }

        if (time.minutes + time.hours > 0) {
            tvClockTimeMiddle.visibility = View.VISIBLE
        } else {
            tvClockTimeMiddle.visibility = View.GONE
        }

        tvClockTimeRight.text = timeRight
    }

    private fun onStateChange() {
        ivClockPauseImage.visibility = if (running != TimeboxState.PAUSE) View.INVISIBLE else View.VISIBLE
    }


}