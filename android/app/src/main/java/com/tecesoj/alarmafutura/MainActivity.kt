package com.tecesoj.alarmafutura

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

class MainActivity : Activity() {
    private lateinit var nameInput: EditText
    private lateinit var selectedDateText: TextView
    private lateinit var alarmsContainer: LinearLayout
    private lateinit var statusText: TextView

    private var selectedTimeMillis: Long =
        System.currentTimeMillis() + 5 * 60 * 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())
        updateSelectedDate()
        requestNotificationPermissionIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        refreshAlarmList()
    }

    private fun buildUi(): ScrollView {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 48)
        }

        root.addView(TextView(this).apply {
            text = "Alarma Futura"
            textSize = 30f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        })

        root.addView(TextView(this).apply {
            text = "Programa una alarma para cualquier fecha y hora futuras."
            textSize = 16f
            setPadding(0, 0, 0, 20)
        })

        nameInput = EditText(this).apply {
            hint = "Nombre de la alarma"
            setSingleLine(true)
        }
        root.addView(nameInput)

        selectedDateText = TextView(this).apply {
            textSize = 18f
            setPadding(0, 28, 0, 12)
        }
        root.addView(selectedDateText)

        root.addView(Button(this).apply {
            text = "Elegir fecha y hora"
            setOnClickListener { pickDateAndTime() }
        })

        root.addView(Button(this).apply {
            text = "Programar alarma"
            setOnClickListener { createAlarm() }
        })

        statusText = TextView(this).apply {
            textSize = 15f
            setPadding(0, 18, 0, 24)
        }
        root.addView(statusText)

        root.addView(TextView(this).apply {
            text = "Alarmas programadas"
            textSize = 22f
            setPadding(0, 24, 0, 16)
        })

        alarmsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(alarmsContainer)

        scroll.addView(root)
        return scroll
    }

    private fun pickDateAndTime() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedTimeMillis
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, day)
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        selectedTimeMillis = calendar.timeInMillis
                        updateSelectedDate()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun createAlarm() {
        val label = nameInput.text.toString().trim()
        if (label.isEmpty()) {
            statusText.text = "Escribe un nombre para la alarma."
            return
        }

        if (selectedTimeMillis <= System.currentTimeMillis() + 30_000L) {
            statusText.text = "Selecciona una fecha y hora futuras."
            return
        }

        if (!AlarmScheduler.hasExactAlarmAccess(this)) {
            AlertDialog.Builder(this)
                .setTitle("Permiso de alarmas exactas")
                .setMessage(
                    "Android necesita permitir que Alarma Futura programe alarmas exactas."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Abrir ajustes") { _, _ ->
                    AlarmScheduler.openExactAlarmSettings(this)
                }
                .show()
            return
        }

        runCatching {
            AlarmScheduler.scheduleNew(this, label, selectedTimeMillis)
        }.onSuccess {
            statusText.text = "Alarma programada."
            nameInput.text.clear()
            selectedTimeMillis = System.currentTimeMillis() + 5 * 60 * 1000L
            updateSelectedDate()
            refreshAlarmList()
        }.onFailure {
            statusText.text = it.message ?: "No fue posible programar la alarma."
        }
    }

    private fun refreshAlarmList() {
        if (!::alarmsContainer.isInitialized) return

        alarmsContainer.removeAllViews()
        val alarms = AlarmStorage.all(this)
            .filter { it.triggerAtMillis > System.currentTimeMillis() }

        if (alarms.isEmpty()) {
            alarmsContainer.addView(TextView(this).apply {
                text = "No hay alarmas programadas."
                textSize = 16f
            })
            return
        }

        alarms.forEach { alarm ->
            val block = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 12, 0, 22)
            }

            block.addView(TextView(this).apply {
                text = alarm.label
                textSize = 18f
            })

            block.addView(TextView(this).apply {
                text = formatDate(alarm.triggerAtMillis)
                textSize = 15f
            })

            block.addView(Button(this).apply {
                text = "Cancelar"
                setOnClickListener {
                    AlarmScheduler.cancel(this@MainActivity, alarm)
                    statusText.text = "Alarma cancelada."
                    refreshAlarmList()
                }
            })

            alarmsContainer.addView(block)
        }
    }

    private fun updateSelectedDate() {
        selectedDateText.text = "Fecha seleccionada: ${formatDate(selectedTimeMillis)}"
    }

    private fun formatDate(timeMillis: Long): String {
        return DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT
        ).format(Date(timeMillis))
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATIONS
            )
        }
    }

    companion object {
        private const val REQUEST_NOTIFICATIONS = 1001
    }
}
