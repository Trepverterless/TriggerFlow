package com.example.macrodroid.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.example.macrodroid.data.Trigger
import com.example.macrodroid.domain.MacroService

class BatteryTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val trigger = Trigger.BatteryTrigger(
                id = intent.getStringExtra("triggerId") ?: "",
                level = level,
                type = if (isCharging) Trigger.BatteryType.CHARGING else Trigger.BatteryType.DISCHARGING
            )

            MacroService.handleTrigger(context, trigger)
        }
    }
}

class BluetoothTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val deviceName = intent.getStringExtra("deviceName") ?: ""
        val isConnected = intent.getBooleanExtra("isConnected", false)

        val trigger = Trigger.BluetoothTrigger(
            id = intent.getStringExtra("triggerId") ?: "",
            deviceName = deviceName,
            type = if (isConnected) Trigger.BluetoothType.CONNECTED else Trigger.BluetoothType.DISCONNECTED
        )

        MacroService.handleTrigger(context, trigger)
    }
}

class ScreenTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val triggerType = when (intent.action) {
            Intent.ACTION_SCREEN_ON -> Trigger.ScreenType.ON
            Intent.ACTION_SCREEN_OFF -> Trigger.ScreenType.OFF
            Intent.ACTION_USER_PRESENT -> Trigger.ScreenType.UNLOCK
            else -> return
        }

        val trigger = Trigger.ScreenTrigger(
            id = intent.getStringExtra("triggerId") ?: "",
            type = triggerType
        )

        MacroService.handleTrigger(context, trigger)
    }
}