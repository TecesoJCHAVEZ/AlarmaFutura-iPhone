package com.tecesoj.alarmafutura

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

class AlarmSoundService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var fallbackRingtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var currentNotificationId: Int = DEFAULT_NOTIFICATION_ID
    private var currentAlarmId: Int = 0
    private var currentLabel: String = "Alarma Futura"
    private var isRinging = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopAlarm()
                return START_NOT_STICKY
            }

            ACTION_START -> {
                currentAlarmId = intent.getIntExtra(EXTRA_ALARM_ID, 0)
                currentLabel = intent.getStringExtra(EXTRA_ALARM_LABEL)
                    .orEmpty()
                    .ifBlank { "Alarma Futura" }

                currentNotificationId =
                    NOTIFICATION_ID_BASE + (currentAlarmId.coerceAtLeast(0) % 10_000)

                val notification = buildNotification(
                    alarmId = currentAlarmId,
                    label = currentLabel
                )

                startAsForeground(notification)

                if (!isRinging) {
                    acquireWakeLock()
                    startVibration()
                    startSound()
                    isRinging = true
                }

                return START_NOT_STICKY
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAsForeground(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                currentNotificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
            )
        } else {
            startForeground(currentNotificationId, notification)
        }
    }

    private fun buildNotification(alarmId: Int, label: String): Notification {
        val fullScreenIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            putExtra("alarm_id", alarmId)
            putExtra("alarm_label", label)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmSoundService::class.java).apply {
            action = ACTION_STOP
            putExtra(EXTRA_ALARM_ID, alarmId)
        }

        val stopPendingIntent = PendingIntent.getService(
            this,
            NOTIFICATION_STOP_REQUEST_BASE + (alarmId.coerceAtLeast(0) % 10_000),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(label)
            .setContentText("La alarma está sonando.")
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setPriority(Notification.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(
                Notification.Action.Builder(
                    R.drawable.ic_alarm,
                    "Detener",
                    stopPendingIntent
                ).build()
            )
            .build()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarmas activas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarmas activas de Alarma Futura"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannel(channel)
    }

    private fun startSound() {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (alarmUri == null) {
            Log.w("AlarmSoundService", "El dispositivo no tiene un tono de alarma predeterminado.")
            return
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmSoundService, alarmUri)
                isLooping = true
                setWakeMode(this@AlarmSoundService, PowerManager.PARTIAL_WAKE_LOCK)
                prepare()
                start()
            }
        } catch (error: Exception) {
            Log.e("AlarmSoundService", "MediaPlayer no pudo reproducir la alarma.", error)
            mediaPlayer?.release()
            mediaPlayer = null
            startFallbackRingtone(alarmUri)
        }
    }

    private fun startFallbackRingtone(uri: android.net.Uri) {
        runCatching {
            fallbackRingtone = RingtoneManager.getRingtone(this, uri)?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    isLooping = true
                }

                play()
            }
        }.onFailure {
            Log.e("AlarmSoundService", "Tampoco se pudo reproducir el tono alternativo.", it)
        }
    }

    private fun startVibration() {
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        val pattern = longArrayOf(
            0,
            900,
            350,
            900,
            350,
            900,
            700
        )

        runCatching {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, 0)
            )
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AlarmaFutura:AlarmSound"
        ).apply {
            setReferenceCounted(false)
            acquire()
        }
    }

    private fun stopAlarm() {
        mediaPlayer?.runCatching {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null

        fallbackRingtone?.stop()
        fallbackRingtone = null

        vibrator?.cancel()
        vibrator = null

        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null

        isRinging = false

        getSystemService(NotificationManager::class.java)
            .cancel(currentNotificationId)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START =
            "com.tecesoj.alarmafutura.action.START_ALARM"
        const val ACTION_STOP =
            "com.tecesoj.alarmafutura.action.STOP_ALARM"

        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_ALARM_LABEL = "alarm_label"

        const val CHANNEL_ID = "alarma_futura_alarmas_activas_v2"

        private const val DEFAULT_NOTIFICATION_ID = 21_000
        private const val NOTIFICATION_ID_BASE = 21_000
        private const val NOTIFICATION_STOP_REQUEST_BASE = 31_000
    }
}
