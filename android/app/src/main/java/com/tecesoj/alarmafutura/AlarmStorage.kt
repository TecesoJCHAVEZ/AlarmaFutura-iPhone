package com.tecesoj.alarmafutura

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class SavedAlarm(
    val id: Int,
    val label: String,
    val triggerAtMillis: Long
)

object AlarmStorage {
    private const val PREFS = "alarma_futura"
    private const val KEY = "saved_alarms"
    private const val KEY_NEXT_ID = "next_alarm_id"

    fun all(context: Context): List<SavedAlarm> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "[]") ?: "[]"
        val array = runCatching { JSONArray(raw) }.getOrElse { JSONArray() }
        val result = mutableListOf<SavedAlarm>()

        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optInt("id", -1)
            val label = item.optString("label", "")
            val trigger = item.optLong("triggerAtMillis", -1L)
            if (id >= 0 && label.isNotBlank() && trigger > 0L) {
                result += SavedAlarm(id, label, trigger)
            }
        }

        return result.sortedBy { it.triggerAtMillis }
    }

    @Synchronized
    fun nextId(context: Context): Int {
        val preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storedNext = preferences.getInt(KEY_NEXT_ID, 1000)
        val nextFromExisting = (all(context).maxOfOrNull { it.id } ?: 999) + 1
        val next = maxOf(storedNext, nextFromExisting)

        check(
            preferences.edit().putInt(KEY_NEXT_ID, next + 1).commit()
        ) {
            "No fue posible reservar un identificador para la alarma."
        }

        return next
    }

    fun upsert(context: Context, alarm: SavedAlarm) {
        val updated = all(context).filterNot { it.id == alarm.id }.toMutableList()
        updated += alarm
        save(context, updated.sortedBy { it.triggerAtMillis })
    }

    fun remove(context: Context, id: Int) {
        save(context, all(context).filterNot { it.id == id })
    }

    fun pruneExpired(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        val future = all(context).filter { it.triggerAtMillis > nowMillis }
        save(context, future)
    }

    private fun save(context: Context, alarms: List<SavedAlarm>) {
        val array = JSONArray()
        alarms.forEach { alarm ->
            array.put(
                JSONObject()
                    .put("id", alarm.id)
                    .put("label", alarm.label)
                    .put("triggerAtMillis", alarm.triggerAtMillis)
            )
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }
}
