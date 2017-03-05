package pl.brokenpipe.timer.screens.timer

import pl.brokenpipe.timer.R
import pl.brokenpipe.timer.TimerMainActivity
import pl.brokenpipe.timer.base.BaseView
import pl.brokenpipe.timer.base.Layout
import pl.brokenpipe.timer.databinding.TimerViewBinding

/**
 * Created by wierzchanowskig on 05.03.2017.
 */
@Layout(R.layout.timer_view)
class TimerView: BaseView<TimerViewBinding>() {
    override fun onViewBound(binding: TimerViewBinding) {
        (activity as TimerMainActivity).goFullScreen()
    }
}