package com.tecesoj.alarmafutura

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var nameInput: EditText
    private lateinit var selectedDateText: TextView
    private lateinit var alarmsContainer: LinearLayout
    private lateinit var alarmCountText: TextView
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
        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(247, 249, 252))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(28), dp(24), dp(40))
        }

        root.addView(ImageView(this).apply {
            setImageResource(R.drawable.alarma_futura_mark)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "Alarma Futura"
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dp(112)
        ))

        root.addView(TextView(this).apply {
            text = "Alarma Futura"
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(20, 36, 64))
            setPadding(0, dp(4), 0, dp(4))
        })

        root.addView(TextView(this).apply {
            text = "Programa varias alarmas para hoy o para cualquier fecha futura."
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(90, 102, 120))
            setPadding(0, 0, 0, dp(10))
        })

        val byRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(22))
        }

        byRow.addView(TextView(this).apply {
            text = "by:"
            textSize = 13f
            setTextColor(Color.rgb(110, 120, 136))
        })

        byRow.addView(ImageView(this).apply {
            setImageResource(R.drawable.omnarel_software_mark)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "OMNAREL Software"
        }, LinearLayout.LayoutParams(dp(32), dp(32)).apply {
            marginStart = dp(7)
            marginEnd = dp(7)
        })

        byRow.addView(TextView(this).apply {
            text = "OMNAREL Software"
            textSize = 13f
            setTextColor(Color.rgb(35, 55, 86))
        })

        root.addView(byRow)

        root.addView(TextView(this).apply {
            text = "Nueva alarma"
            textSize = 21f
            setTextColor(Color.rgb(20, 36, 64))
            setPadding(0, 0, 0, dp(8))
        })

        nameInput = EditText(this).apply {
            hint = "Nombre de la alarma"
            setSingleLine(true)
        }
        root.addView(nameInput)

        selectedDateText = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.rgb(35, 55, 86))
            setPadding(0, dp(22), 0, dp(10))
        }
        root.addView(selectedDateText)

        root.addView(Button(this).apply {
            text = "Elegir fecha"
            setOnClickListener { pickDate() }
        })

        root.addView(Button(this).apply {
            text = "Elegir hora"
            setOnClickListener { pickTime() }
        })

        root.addView(TextView(this).apply {
            text = "Puedes crear varias alarmas el mismo día. Para hoy, selecciona una hora y minutos posteriores al momento actual."
            textSize = 13f
            setTextColor(Color.rgb(100, 110, 126))
            setPadding(0, dp(8), 0, dp(8))
        })

        root.addView(Button(this).apply {
            text = "Programar alarma"
            setOnClickListener { createAlarm() }
        })

        statusText = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.rgb(55, 80, 112))
            setPadding(0, dp(14), 0, dp(24))
        }
        root.addView(statusText)

        val listHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        listHeader.addView(TextView(this).apply {
            text = "Alarmas programadas"
            textSize = 21f
            setTextColor(Color.rgb(20, 36, 64))
        }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        alarmCountText = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.rgb(100, 110, 126))
        }
        listHeader.addView(alarmCountText)

        root.addView(listHeader)

        alarmsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, 0)
        }
        root.addView(alarmsContainer)

        scroll.addView(root)
        return scroll
    }

    private fun pickDate() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedTimeMillis
        }

        val dialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    val fallback = Calendar.getInstance().apply {
                        add(Calendar.MINUTE, 5)
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, fallback.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, fallback.get(Calendar.MINUTE))
                }

                selectedTimeMillis = calendar.timeInMillis
                updateSelectedDate()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        dialog.datePicker.minDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        dialog.show()
    }

    private fun pickTime() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedTimeMillis
        }

        TimePickerDialog(
            this,
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                selectedTimeMillis = calendar.timeInMillis
                updateSelectedDate()

                if (selectedTimeMillis <= System.currentTimeMillis() + 30_000L) {
                    statusText.text = "Para hoy, elige una hora y minutos posteriores al momento actual."
                } else {
                    statusText.text = ""
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun createAlarm() {
        val label = nameInput.text.toString().trim()
        if (label.isEmpty()) {
            statusText.text = "Escribe un nombre para la alarma."
            return
        }

        if (selectedTimeMillis <= System.currentTimeMillis() + 30_000L) {
            statusText.text = "Para hoy, elige una hora y minutos posteriores al momento actual."
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
            statusText.text = "Alarma programada. Puedes agregar otra sin perder las anteriores."
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

        AlarmStorage.pruneExpired(this)
        alarmsContainer.removeAllViews()

        val alarms = AlarmStorage.all(this)
        alarmCountText.text = alarms.size.toString()

        if (alarms.isEmpty()) {
            alarmsContainer.addView(TextView(this).apply {
                text = "No hay alarmas programadas."
                textSize = 16f
                setTextColor(Color.rgb(100, 110, 126))
                setPadding(0, dp(10), 0, dp(10))
            })
            return
        }

        alarms.forEach { alarm ->
            val block = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), dp(14), dp(18), dp(14))
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dp(16).toFloat()
                    setStroke(dp(1), Color.rgb(224, 230, 238))
                }
            }

            block.addView(TextView(this).apply {
                text = alarm.label
                textSize = 18f
                setTextColor(Color.rgb(20, 36, 64))
            })

            block.addView(TextView(this).apply {
                text = formatDate(alarm.triggerAtMillis)
                textSize = 15f
                setTextColor(Color.rgb(90, 102, 120))
                setPadding(0, dp(4), 0, dp(8))
            })

            block.addView(Button(this).apply {
                text = "Cancelar"
                setOnClickListener {
                    AlarmScheduler.cancel(this@MainActivity, alarm)
                    statusText.text = "Alarma cancelada."
                    refreshAlarmList()
                }
            })

            alarmsContainer.addView(
                block,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(10)
                }
            )
        }
    }

    private fun updateSelectedDate() {
        selectedDateText.text = "Fecha seleccionada: ${formatDate(selectedTimeMillis)}"
    }

    private fun formatDate(timeMillis: Long): String {
        val target = Calendar.getInstance().apply { this.timeInMillis = timeMillis }
        val now = Calendar.getInstance()

        val sameDay =
            target.get(Calendar.ERA) == now.get(Calendar.ERA) &&
            target.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            target.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

        return if (sameDay) {
            val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timeMillis))
            "Hoy · $time"
        } else {
            DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT
            ).format(Date(timeMillis))
        }
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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        private const val REQUEST_NOTIFICATIONS = 1001
    }
}
