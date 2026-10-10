package com.tecesoj.alarmafutura;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

public final class AlarmScheduler {
    public static final String ACTION_FIRE = "com.tecesoj.alarmafutura.FIRE";
    private AlarmScheduler() {}

    public static boolean canSchedule(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms();
    }

    public static void schedule(Context context, AlarmRepository.Entry entry) {
        if (!canSchedule(context)) {
            throw new SecurityException("Autoriza Alarmas y recordatorios en Ajustes.");
        }
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent fire = fireIntent(context, entry.id, entry.name, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent open = new Intent(context, MainActivity.class);
        PendingIntent show = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        manager.setAlarmClock(new AlarmManager.AlarmClockInfo(entry.time, show), fire);
    }

    public static void cancel(Context context, int id) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent fire = fireIntent(context, id, "", PendingIntent.FLAG_NO_CREATE);
        if (fire != null) {
            manager.cancel(fire);
            fire.cancel();
        }
    }

    private static PendingIntent fireIntent(Context context, int id, String name, int flags) {
        Intent intent = new Intent(context, AlarmReceiver.class)
                .setAction(ACTION_FIRE)
                .setData(Uri.parse("alarmfutura://alarm/" + id))
                .putExtra("id", id)
                .putExtra("name", name);
        return PendingIntent.getBroadcast(context, id, intent, flags | PendingIntent.FLAG_IMMUTABLE);
    }
}
