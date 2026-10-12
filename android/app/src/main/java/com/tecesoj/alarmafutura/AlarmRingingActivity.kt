package com.tecesoj.alarmafutura

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class AlarmRingingActivity : Activity() {
    private var alarmId: Int = 0
    private var alarmLabel: String = "Alarma Futura"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        )

        alarmId = intent.getIntExtra("alarm_id", 0)
        alarmLabel = intent.getStringExtra("alarm_label")
            .orEmpty()
            .ifBlank { "Alarma Futura" }

        ensureAlarmServiceIsRunning()

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
            text = "ALARMA"
            textSize = 20f
            gravity = Gravity.CENTER
        })

        root.addView(TextView(this).apply {
            text = alarmLabel
            textSize = 34f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 48)
        })

        root.addView(Button(this).apply {
            text = "Detener"
            setOnClickListener {
                stopAlarmAndFinish()
            }
        })

        setContentView(root)
    }

    private fun ensureAlarmServiceIsRunning() {
        val serviceIntent = Intent(this, AlarmSoundService::class.java).apply {
            action = AlarmSoundService.ACTION_START
            putExtra(AlarmSoundService.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmSoundService.EXTRA_ALARM_LABEL, alarmLabel)
        }

        runCatching {
            startForegroundService(serviceIntent)
        }
    }

    private fun stopAlarmAndFinish() {
        val stopIntent = Intent(this, AlarmSoundService::class.java).apply {
            action = AlarmSoundService.ACTION_STOP
            putExtra(AlarmSoundService.EXTRA_ALARM_ID, alarmId)
        }

        runCatching {
            startService(stopIntent)
        }

        finishAndRemoveTask()
    }
}
