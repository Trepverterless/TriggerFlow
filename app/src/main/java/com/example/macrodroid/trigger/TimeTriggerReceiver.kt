package com.example.macrodroid.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.macrodroid.data.Trigger
import com.example.macrodroid.domain.MacroService
import java.util.Calendar

class TimeTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        val days = intent.getIntArrayExtra("days")?.toList() ?: emptyList()

        if (hour != -1 && minute != -1) {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)
            val currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1

            if (currentHour == hour && currentMinute == minute && days.contains(currentDay)) {
                val trigger = Trigger.TimeTrigger(
                    id = intent.getStringExtra("triggerId") ?: "",
                    hour = hour,
                    minute = minute,
                    days = days
                )
                MacroService.handleTrigger(context, trigger)
            }
        }
    }

    companion object {
        private val scheduledTriggers = mutableSetOf<String>()

        fun scheduleTrigger(context: Context, trigger: Trigger.TimeTrigger) {
            val intent = Intent(context, TimeTriggerReceiver::class.java).apply {
                putExtra("hour", trigger.hour)
                putExtra("minute", trigger.minute)
                putExtra("days", trigger.days.toIntArray())
                putExtra("triggerId", trigger.id)
            }

            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                trigger.hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, trigger.hour)
                set(Calendar.MINUTE, trigger.minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                scheduledTriggers.add(trigger.id)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }

        fun cancelTrigger(context: Context, trigger: Trigger.TimeTrigger) {
            val intent = Intent(context, TimeTriggerReceiver::class.java).apply {
                putExtra("hour", trigger.hour)
                putExtra("minute", trigger.minute)
                putExtra("days", trigger.days.toIntArray())
                putExtra("triggerId", trigger.id)
            }

            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context,
                trigger.hashCode(),
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.cancel(pendingIntent)
            scheduledTriggers.remove(trigger.id)
        }

        fun cancelAllTriggers(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            
            scheduledTriggers.forEach { triggerId ->
                val intent = Intent(context, TimeTriggerReceiver::class.java)
                val pendingIntent = android.app.PendingIntent.getBroadcast(
                    context,
                    triggerId.hashCode(),
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.cancel(pendingIntent)
            }
            scheduledTriggers.clear()
        }
    }
}