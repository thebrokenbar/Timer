package pl.brokenpipe.timer.screens.timer

import android.media.AudioManager
import android.media.SoundPool
import kotlinx.android.synthetic.main.timer_view.clockFace
import pl.brokenpipe.timer.R
import pl.brokenpipe.timer.TimerMainActivity
import pl.brokenpipe.timer.base.BaseView
import pl.brokenpipe.timer.base.Layout
import pl.brokenpipe.timer.databinding.TimerViewBinding
import rx.Observable
import timber.log.Timber

@Layout(R.layout.timer_view)
class TimerView : BaseView<TimerViewBinding>(), TimerViewActions {

    lateinit var viewModel: TimerViewModel
    @Suppress("DEPRECATION")
    val soundPool: SoundPool = if (android.os.Build.VERSION.SDK_INT >= 21) SoundPool.Builder().build()
    else SoundPool(2, AudioManager.STREAM_MUSIC, 0)

    var soundId: Int = 0

    override fun startTimer() {
        activity.clockFace.start()
    }

    override fun pauseTimer() {
        activity.clockFace.pause()
    }

    override fun onViewBound(binding: TimerViewBinding) {
//        (activity as TimerMainActivity).goFullScreen()
        viewModel = TimerViewModel(this)
        binding.viewModel = viewModel
        soundId = soundPool.load(activity, R.raw.alarm2, 1)
    }

    override fun getTimerSecondsObservable(): Observable<Long> {
        return activity.clockFace.getTimerObservable()
    }

    override fun playEndSound() {
        val playResult = soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        Timber.i("Sound result with: %d", playResult)
    }

    override fun stopEndSound() {
        soundPool.stop(soundId)
    }
}