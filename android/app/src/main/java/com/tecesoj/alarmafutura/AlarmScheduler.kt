package com.tecesoj.alarmafutura

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object AlarmScheduler {
    fun hasExactAlarmAccess(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val manager = context.getSystemService(AlarmManager::class.java)
        return manager.canScheduleExactAlarms()
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val intent = Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun scheduleNew(context: Context, label: String, triggerAtMillis: Long): SavedAlarm {
        check(triggerAtMillis > System.currentTimeMillis() + 30_000L) {
            "La fecha y hora de la alarma deben estar en el futuro."
        }
        check(hasExactAlarmAccess(context)) {
            "Android no ha autorizado alarmas exactas."
        }

        val id = AlarmStorage.nextId(context)
        val alarm = SavedAlarm(id, label, triggerAtMillis)
        schedule(context, alarm)
        AlarmStorage.upsert(context, alarm)
        return alarm
    }

    fun schedule(context: Context, alarm: SavedAlarm) {
        val manager = context.getSystemService(AlarmManager::class.java)

        val fireIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_id", alarm.id)
            putExtra("alarm_label", alarm.label)
        }

        val firePendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            fireIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = PendingIntent.getActivity(
            context,
            alarm.id,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        manager.setAlarmClock(
            AlarmManager.AlarmClockInfo(alarm.triggerAtMillis, showIntent),
            firePendingIntent
        )
    }

    fun cancel(context: Context, alarm: SavedAlarm) {
        val manager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            manager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        AlarmStorage.remove(context, alarm.id)
    }

    fun rescheduleFuture(context: Context) {
        if (!hasExactAlarmAccess(context)) return
        val now = System.currentTimeMillis()
        AlarmStorage.all(context)
            .filter { it.triggerAtMillis > now }
            .forEach { schedule(context, it) }
    }
}
