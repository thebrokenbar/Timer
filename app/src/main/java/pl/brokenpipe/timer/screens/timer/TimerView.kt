package pl.brokenpipe.timer.screens.timer

import android.graphics.Interpolator
import android.media.AudioManager
import android.media.SoundPool
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import kotlinx.android.synthetic.main.timer_view.*
import pl.brokenpipe.timer.R
import pl.brokenpipe.timer.TimerMainActivity
import pl.brokenpipe.timer.base.BaseView
import pl.brokenpipe.timer.base.Layout
import pl.brokenpipe.timer.databinding.TimerViewBinding
import rx.Observable
import timber.log.Timber

@Layout(R.layout.timer_view)
class TimerView : BaseView<TimerViewBinding>(), TimerViewActions {

    private var timeFlowAnimation = AnimationSet(true)

    lateinit var viewModel: TimerViewModel
    @Suppress("DEPRECATION")
    val soundPool: SoundPool = if (android.os.Build.VERSION.SDK_INT >= 21) SoundPool.Builder().build()
    else SoundPool(2, AudioManager.STREAM_MUSIC, 0)

    var soundId: Int = 0

    init {
        with(timeFlowAnimation) {
            val animationDuration = 800L
            interpolator = DecelerateInterpolator()
            val scale = ScaleAnimation(
                1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f)
            scale.duration = animationDuration

            val fade = AlphaAnimation(1f, 0f)
            fade.duration = animationDuration

            addAnimation(scale)
            addAnimation(fade)

            duration = animationDuration
            repeatCount = 1
        }
    }

    override fun startTimer() {
        activity.clockFace.start()
    }

    override fun pauseTimer() {
        activity.clockFace.pause()
        activity.rlClockCenter.clearAnimation()

    }

    override fun animateTimeFlow() {
        activity.vClockCenterBackground.startAnimation(timeFlowAnimation)
    }

    override fun onViewBound(binding: TimerViewBinding) {
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

    override fun keepScreenOn() {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun letScreenOff() {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}