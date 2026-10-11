package com.tecesoj.alarmafutura

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AlarmRingingActivity : Activity() {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var notificationId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        notificationId = intent.getIntExtra("alarm_id", 0)
        val label = intent.getStringExtra("alarm_label").orEmpty().ifBlank { "Alarma Futura" }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        root.addView(TextView(this).apply {
            text = "ALARMА"
            textSize = 20f
            gravity = Gravity.CENTER
        })

        root.addView(TextView(this).apply {
            text = label
            textSize = 34f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 48)
        })

        root.addView(Button(this).apply {
            text = "Detener"
            setOnClickListener { stopAlarmAndFinish() }
        })

        setContentView(root)
        startAlarmSound()
    }

    private fun startAlarmSound() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        ringtone = RingtoneManager.getRingtone(this, uri)?.apply {
            audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                isLooping = true
            }
            play()
        }

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator?.vibrate(
            VibrationEffect.createWaveform(
                longArrayOf(0, 700, 350, 700, 350),
                0
            )
        )
    }

    private fun stopAlarmAndFinish() {
        ringtone?.stop()
        vibrator?.cancel()
        getSystemService(NotificationManager::class.java).cancel(notificationId)
        finishAndRemoveTask()
    }

    override fun onDestroy() {
        if (isFinishing) {
            ringtone?.stop()
            vibrator?.cancel()
        }
        super.onDestroy()
    }
}
