package pl.brokenpipe.timer.screens.timer

import android.view.View
import pl.brokenpipe.timer.R
import pl.brokenpipe.timer.TimerMainActivity
import pl.brokenpipe.timer.base.BaseView
import pl.brokenpipe.timer.base.Layout
import pl.brokenpipe.timer.databinding.TimerViewBinding
import kotlinx.android.synthetic.main.timer_view.*
import rx.Observable

/**
 * Created by wierzchanowskig on 05.03.2017.
 */
@Layout(R.layout.timer_view)
class TimerView: BaseView<TimerViewBinding>(), TimerViewActions {
    lateinit var viewModel: TimerViewModel

    override fun startTimer() {
        activity.clockFace.start()
    }

    override fun pauseTimer() {
        activity.clockFace.pause()
    }

    override fun onViewBound(binding: TimerViewBinding) {
        (activity as TimerMainActivity).goFullScreen()
        viewModel = TimerViewModel(this)
        binding.viewModel = viewModel
    }

    override fun getTimerSecondsObservable(): Observable<Long> {
        return activity.clockFace.getOnTimeChangeObservable()
    }
}