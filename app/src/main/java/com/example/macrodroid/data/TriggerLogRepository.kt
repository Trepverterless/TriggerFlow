package com.example.macrodroid.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TriggerLogRepository private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("trigger_logs", Context.MODE_PRIVATE)
    private val _logs = MutableStateFlow<List<TriggerLog>>(emptyList())
    val logs: StateFlow<List<TriggerLog>> = _logs.asStateFlow()

    companion object {
        @Volatile
        private var instance: TriggerLogRepository? = null

        fun getInstance(context: Context): TriggerLogRepository {
            return instance ?: synchronized(this) {
                instance ?: TriggerLogRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    init {
        loadLogs()
    }

    fun addLog(log: TriggerLog) {
        val currentLogs = _logs.value.toMutableList()
        currentLogs.add(0, log) // Add to the beginning
        // Keep only the last 100 logs
        if (currentLogs.size > 100) {
            currentLogs.removeAt(currentLogs.size - 1)
        }
        _logs.value = currentLogs
        saveLogs(currentLogs)
    }

    fun clearLogs() {
        _logs.value = emptyList()
        saveLogs(emptyList())
    }

    private fun loadLogs() {
        val logsString = sharedPreferences.getString("logs", null)
        if (logsString != null) {
            try {
                val logs = logsString.split("\n").filter { it.isNotBlank() }.map { line ->
                    val parts = line.split("||")
                    val actions = if (parts.size > 5) {
                        parts[5].split("&&").filter { it.isNotBlank() }
                    } else {
                        emptyList()
                    }
                    TriggerLog(
                        id = parts[0].toLong(),
                        macroId = parts[1].toInt(),
                        macroName = parts[2],
                        event = parts[3],
                        actions = actions,
                        timestamp = parts[4].toLong()
                    )
                }
                _logs.value = logs
            } catch (e: Exception) {
                _logs.value = emptyList()
            }
        }
    }

    private fun saveLogs(logs: List<TriggerLog>) {
        val logsString = logs.joinToString("\n") { log ->
            val actionsString = log.actions.joinToString("&&")
            "${log.id}||${log.macroId}||${log.macroName}||${log.event}||${log.timestamp}||$actionsString"
        }
        sharedPreferences.edit().putString("logs", logsString).apply()
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}