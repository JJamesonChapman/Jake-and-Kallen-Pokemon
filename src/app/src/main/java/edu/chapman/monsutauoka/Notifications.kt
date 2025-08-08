package edu.chapman.monsutauoka
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.chapman.monsutauoka.extensions.TAG

class ReminderReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        Log.v(TAG, "Received")
        NotifChannels.ensure(context)

        val title   = intent.getStringExtra("title")   ?: "Reminder"
        val message = intent.getStringExtra("message") ?: "It's time"
        val tab     = intent.getStringExtra("open_tab") // optional
        val notifId = intent.getIntExtra("notif_id", (System.currentTimeMillis()/1000).toInt())

        // Activity to open when user taps the notification
        val launch = Intent(context, EntryActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tab", tab)
        }
        val contentPI = PendingIntent.getActivity(
            context,
            notifId,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotifChannels.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(contentPI)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notification)
    }
}

object NotifChannels {
    const val REMINDER_CHANNEL_ID = "reminders"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Scheduled reminders from the app" }
            mgr.createNotificationChannel(channel)
        }
    }
}

object ReminderScheduler {
    fun scheduleExactAt(
        context: Context,
        triggerAtMillis: Long,
        title: String,
        message: String,
        openTab: String? = null,
        requestCode: Int = (System.currentTimeMillis()/1000).toInt()
    ): Int {
        Log.v(TAG, "Scheduled in scheduler")
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("open_tab", openTab)
            putExtra("notif_id", requestCode)
        }

        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmMgr.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        return requestCode // keep so you can cancel later
    }

    fun cancel(context: Context, requestCode: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmMgr.cancel(pi)
        pi.cancel()
    }
}

