package pl.brokenpipe.timeboxing

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.RouterTransaction
import pl.brokenpipe.timeboxing.screens.timer.TimerController
import pl.brokenpipe.timeboxing.R.id
import pl.brokenpipe.timeboxing.R.layout
import pl.brokenpipe.timeboxing.screens.timer.TestTime
import timber.log.Timber
import timber.log.Timber.DebugTree

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class TimerMainActivity : AppCompatActivity() {
    lateinit var container: ViewGroup
    lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(DebugTree())
        setContentView(layout.activity_timer_main)
        container = findViewById(id.conductorContainer) as ViewGroup

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(TestTime()))
        }
    }


    override fun onResume() {
        super.onResume()
        goFullScreen()
    }

    @SuppressLint("InlinedApi")
    fun goFullScreen() {
        supportActionBar?.hide()
        container.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
}
