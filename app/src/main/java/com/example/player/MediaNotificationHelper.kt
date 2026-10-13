package com.example.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Zamil

object MediaNotificationHelper {
    const val CHANNEL_PLAYBACK_ID = "zawamil_playback_channel"
    const val CHANNEL_NEW_ZAMIL_ID = "zawamil_new_releases_channel"
    const val NOTIFICATION_PLAYBACK_ID = 1001
    const val NOTIFICATION_NEW_ZAMIL_ID = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val playbackChannel = NotificationChannel(
                CHANNEL_PLAYBACK_ID,
                "تشغيل الزوامل",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعارات مشغل الصوت في الخلفية"
            }

            val newZamilChannel = NotificationChannel(
                CHANNEL_NEW_ZAMIL_ID,
                "إشعارات الزوامل الجديدة",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعار عند نشر زوامل جديدة"
            }

            notificationManager.createNotificationChannel(playbackChannel)
            notificationManager.createNotificationChannel(newZamilChannel)
        }
    }

    fun showNewZamilNotification(context: Context, zamil: Zamil) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_ZAMIL_ID", zamil.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_NEW_ZAMIL_ID)
            .setSmallIcon(R.drawable.app_icon_foreground)
            .setContentTitle("زامل جديد: ${zamil.title}")
            .setContentText("للمنشد ${zamil.performer} - استمع الآن")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_NEW_ZAMIL_ID, notification)
    }
}
