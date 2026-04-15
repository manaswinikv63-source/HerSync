package com.example.hersync.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class DailyLogEntry(
    val dateEpochDay: Long,
    val mood: String,
    val symptoms: List<String>,
    val flow: String,
    val notes: String,
    val updatedAtMillis: Long = System.currentTimeMillis(),
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("dateEpochDay", dateEpochDay)
        put("mood", mood)
        put("symptoms", JSONArray(symptoms))
        put("flow", flow)
        put("notes", notes)
        put("updatedAtMillis", updatedAtMillis)
    }

    companion object {
        fun fromJson(o: JSONObject): DailyLogEntry = DailyLogEntry(
            dateEpochDay = o.getLong("dateEpochDay"),
            mood = o.getString("mood"),
            symptoms = o.getJSONArray("symptoms").let { arr ->
                List(arr.length()) { i -> arr.getString(i) }
            },
            flow = o.getString("flow"),
            notes = o.getString("notes"),
            updatedAtMillis = o.optLong("updatedAtMillis", System.currentTimeMillis()),
        )
    }
}

class LogRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun loadAscending(): List<DailyLogEntry> {
        val raw = prefs.getString(KEY_LOGS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    add(DailyLogEntry.fromJson(arr.getJSONObject(i)))
                }
            }.sortedBy { it.dateEpochDay }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getAllAscending(): List<DailyLogEntry> = loadAscending()

    /** Newest days first (for history list). */
    fun getAllDescending(): List<DailyLogEntry> = loadAscending().sortedByDescending { it.dateEpochDay }

    fun getForDateEpochDay(epochDay: Long): DailyLogEntry? =
        loadAscending().firstOrNull { it.dateEpochDay == epochDay }

    fun upsert(entry: DailyLogEntry) {
        val next = loadAscending().filter { it.dateEpochDay != entry.dateEpochDay } + entry
        val arr = JSONArray()
        next.sortedBy { it.dateEpochDay }.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_LOGS, arr.toString()).apply()
    }

    fun replaceAll(entries: List<DailyLogEntry>) {
        val arr = JSONArray()
        entries.sortedBy { it.dateEpochDay }.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY_LOGS, arr.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "hersync_daily_logs"
        private const val KEY_LOGS = "entries_v1"
    }
}
