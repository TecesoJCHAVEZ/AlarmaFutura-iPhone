package com.tecesoj.alarmafutura;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (AlarmScheduler.ACTION_FIRE.equals(action)) {
            int id = intent.getIntExtra("id", -1);
            if (id < 0) return;
            AlarmRepository.remove(context, id);
            Intent ring = new Intent(context, RingService.class)
                    .putExtra("name", intent.getStringExtra("name"));
            try {
                context.startForegroundService(ring);
            } catch (Exception error) {
                // No fingir éxito si el sistema impide iniciar el servicio.
                android.util.Log.e("AlarmaFutura", "No se pudo iniciar la alarma", error);
            }
            return;
        }

        boolean reschedule = Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || (Build.VERSION.SDK_INT >= 31
                    && AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED.equals(action));
        if (!reschedule || !AlarmScheduler.canSchedule(context)) return;

        for (AlarmRepository.Entry entry : AlarmRepository.list(context)) {
            if (entry.time <= System.currentTimeMillis()) {
                AlarmRepository.remove(context, entry.id);
                continue;
            }
            try {
                AlarmScheduler.schedule(context, entry);
            } catch (SecurityException error) {
                android.util.Log.w("AlarmaFutura", "Sin permiso para reprogramar", error);
            }
        }
    }
}
