package pl.brokenpipe.timeboxing.screens.timer

import android.animation.ValueAnimator
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Typeface
import android.media.AudioManager
import android.media.SoundPool
import android.media.SoundPool.Builder
import android.os.Build.VERSION
import android.view.View
import android.view.WindowManager.LayoutParams
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import kotlinx.android.synthetic.main.timer_view.clockFace
import kotlinx.android.synthetic.main.timer_view.rlClockCenter
import kotlinx.android.synthetic.main.timer_view.tvClockTimeLeft
import kotlinx.android.synthetic.main.timer_view.tvClockTimeMiddle
import kotlinx.android.synthetic.main.timer_view.tvClockTimeRight
import kotlinx.android.synthetic.main.timer_view.vClockCenterBackground
import pl.brokenpipe.boundcontroller.BoundController
import pl.brokenpipe.boundcontroller.Layout
import pl.brokenpipe.timeboxing.R.layout
import pl.brokenpipe.timeboxing.R.raw
import pl.brokenpipe.timeboxing.databinding.TimerViewBinding
import pl.brokenpipe.timeboxing.notification.TimerNotification
import pl.brokenpipe.timeboxing.persistance.SharedPreferencesDataManager
import pl.brokenpipe.timeboxing.persistance.SimpleDataManager
import rx.Observable
import rx.Subscription
import timber.log.Timber

@Layout(layout.timer_view)
class TimerController : BoundController<TimerViewBinding>(), TimerViewActions {

    lateinit private var notification: TimerNotification
    private val SOUND_FADE_OUT_DURATION = 200L
    private var timeFlowAnimation = AnimationSet(true)

    var viewModel: TimerViewModel? = null
    @Suppress("DEPRECATION")
    private val soundPool: SoundPool = if (VERSION.SDK_INT >= 21)
        Builder().build()
    else SoundPool(2, AudioManager.STREAM_MUSIC, 0)

    private var soundId: Int = 0
    private var subscription: Subscription? = null
    private lateinit var simpleDataManager: SimpleDataManager

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

    override fun onActivityStarted(activity: Activity?) {
        super.onActivityStarted(activity)
        notification = TimerNotification(
                applicationContext.getSystemService(
                        Context.NOTIFICATION_SERVICE) as NotificationManager)
        cancelNotification()
    }


    override fun onDestroyView(view: View?) {
        super.onDestroyView(view)
        cancelNotification()
    }

    private fun cancelNotification() {
        notification.dismiss()
        subscription?.unsubscribe()
    }

    override fun onActivityStopped(activity: Activity) {
        super.onActivityStopped(activity)
        showNotification(activity)
    }

    private fun showNotification(activity: Activity) {
        subscription = getTimerSecondsObservable().subscribe(
                {
                    if (viewModel != null) {
                        notification.show(viewModel!!.time, activity)
                    }
                })
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
        if (viewModel == null) {
            viewModel = TimerViewModel(this)
        }

        binding.viewModel = viewModel
        binding.viewModel.subscribeChanges()
        soundId = soundPool.load(activity, raw.alarm3, 1)
        binding.viewModel.subscribeClockState(activity.clockFace.getStateObservable())
        if (!binding.viewModel.time.isZero()) {
            activity.clockFace.setTime(binding.viewModel.time.getTotalSeconds())
            startTimer()
        }
        setupFonts()
        activity.clockFace.setSide(binding.viewModel.clockSpinSide)

        simpleDataManager = SharedPreferencesDataManager(activity.getSharedPreferences("sharedPref", MODE_PRIVATE))
        binding.viewModel.showOnboarding = isOnboardingVisible()

    }

    override fun onViewUnbound(binding: TimerViewBinding) {
        activity.clockFace.dispose()
        binding.viewModel.clockSpinSide = activity.clockFace.getSide()
        binding.viewModel.dispose()
    }

    private fun setupFonts() {
        val colonTypeface = Typeface.createFromAsset(activity.assets, "Roboto-Thin.ttf")
        activity.tvClockTimeMiddle.typeface = colonTypeface
        activity.tvClockTimeLeft.setTypeface(activity.tvClockTimeRight.typeface, Typeface.BOLD)
        activity.tvClockTimeRight.setTypeface(activity.tvClockTimeRight.typeface, Typeface.BOLD)
    }

    override fun getTimerSecondsObservable(): Observable<Long> {
        return activity.clockFace.getTimerObservable()
    }

    override fun playEndSound() {
        val playResult = soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        Timber.i("Sound result with: %d", playResult)
    }

    override fun stopEndSound() {
        val animation = ValueAnimator.ofFloat(1f, 0f)
        animation.duration = SOUND_FADE_OUT_DURATION
        animation.repeatCount = 1
        animation.addUpdateListener {
            soundPool.setVolume(soundId, it.animatedValue as Float, it.animatedValue as Float)
            if (it.animatedValue as Float <= 0) {
                it.removeAllUpdateListeners()
            }
        }
        soundPool.stop(soundId)
    }

    override fun keepScreenOn() {
        activity.window.addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun letScreenOff() {
        activity.window.clearFlags(LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun isOnboardingVisible(): Boolean {
        return simpleDataManager.getValue("isOnboardingVisible", true, Boolean::class.java)
    }


}