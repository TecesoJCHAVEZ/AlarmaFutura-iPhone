package com.tecesoj.alarmafutura;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

public class RingService extends Service {
    public static final String ACTION_STOP = "com.tecesoj.alarmafutura.STOP";
    private static final String CHANNEL = "alarm_ringing_v1";
    private static final int NOTIFICATION_ID = 8001;
    private MediaPlayer player;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }
        String name = intent == null ? null : intent.getStringExtra("name");
        if (name == null || name.trim().isEmpty()) name = "Alarma Futura";

        NotificationManager manager = getSystemService(NotificationManager.class);
        NotificationChannel channel = new NotificationChannel(
                CHANNEL, "Alarmas activas", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Alarmas programadas por el usuario.");
        channel.setSound(null, null); // El audio repetido lo gestiona el servicio.
        manager.createNotificationChannel(channel);

        Intent display = new Intent(this, RingActivity.class).putExtra("name", name)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent show = PendingIntent.getActivity(this, 10, display,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent stop = new Intent(this, RingService.class).setAction(ACTION_STOP);
        PendingIntent stopAction = PendingIntent.getService(this, 11, stop,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(name)
                .setContentText("Alarma en curso. Pulsa Detener para silenciar.")
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setContentIntent(show)
                .setFullScreenIntent(show, true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Detener", stopAction)
                .build();

        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        startSound();
        return START_NOT_STICKY;
    }

    private void startSound() {
        if (player != null) return;
        try {
            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (uri == null) uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer media = new MediaPlayer();
            media.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
            media.setDataSource(this, uri);
            media.setLooping(true);
            media.prepare();
            media.start();
            player = media;
        } catch (Exception error) {
            Log.e("AlarmaFutura", "No se pudo reproducir sonido", error);
        }
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            if (player.isPlaying()) player.stop();
            player.release();
            player = null;
        }
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
