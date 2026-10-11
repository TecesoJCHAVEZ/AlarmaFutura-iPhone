package com.tecesoj.alarmafutura

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("alarm_id", 0)
        val label = intent.getStringExtra("alarm_label").orEmpty().ifBlank { "Alarma Futura" }

        AlarmStorage.remove(context, id)

        val manager = context.getSystemService(NotificationManager::class.java)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarmas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarmas programadas por Alarma Futura"
            enableVibration(true)
            setSound(
                sound,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
            )
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)

        val fullScreenIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra("alarm_id", id)
            putExtra("alarm_label", label)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            id,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.tecesoj.alarmafutura.R.drawable.ic_alarm)
            .setContentTitle(label)
            .setContentText("Es hora de tu alarma.")
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setPriority(Notification.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()

        manager.notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "alarma_futura_alarmas"
    }
}
