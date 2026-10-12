package com.tecesoj.alarmafutura

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("alarm_id", 0)
        val label = intent.getStringExtra("alarm_label")
            .orEmpty()
            .ifBlank { "Alarma Futura" }

        val serviceIntent = Intent(context, AlarmSoundService::class.java).apply {
            action = AlarmSoundService.ACTION_START
            putExtra(AlarmSoundService.EXTRA_ALARM_ID, id)
            putExtra(AlarmSoundService.EXTRA_ALARM_LABEL, label)
        }

        try {
            context.startForegroundService(serviceIntent)
            AlarmStorage.remove(context, id)
        } catch (error: Exception) {
            Log.e("AlarmReceiver", "No se pudo iniciar la alarma en primer plano.", error)

            val fallback = Intent(context, AlarmRingingActivity::class.java).apply {
                putExtra("alarm_id", id)
                putExtra("alarm_label", label)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            runCatching { context.startActivity(fallback) }
        }
    }
}
