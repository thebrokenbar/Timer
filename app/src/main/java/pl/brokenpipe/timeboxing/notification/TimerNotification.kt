package pl.brokenpipe.timeboxing.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.support.v7.app.NotificationCompat
import pl.brokenpipe.timeboxing.R
import pl.brokenpipe.timeboxing.screens.timer.Time
import android.app.PendingIntent
import android.content.Intent
import pl.brokenpipe.timeboxing.TimerMainActivity


class TimerNotification(val notificationManager: NotificationManager) {
    companion object {
        val NOTIFICATION_ID = 1
    }

    private fun create(time: Time, context: Context): Notification {
        val notificationIntent = Intent(context, TimerMainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)

        return NotificationCompat.Builder(context)
            .setContentText("%d:%02d:%02d".format(time.hours, time.minutes, time.seconds))
            .setContentTitle(context.getString(R.string.app_name))
            .setOngoing(true)
            .setDefaults(0)
            .setPriority(0)
            .setContentIntent(contentIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    fun show(time: Time, context: Context) {
        val notification = create(time, context)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun dismiss() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

}