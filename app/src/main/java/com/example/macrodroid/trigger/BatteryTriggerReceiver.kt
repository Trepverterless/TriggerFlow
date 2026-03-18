package com.example.macrodroid.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import com.example.macrodroid.data.Trigger
import com.example.macrodroid.domain.MacroService

class BatteryTriggerReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BatteryTriggerReceiver"
        
        /**
         * 注册电池状态监听器
         */
        fun register(context: Context) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BATTERY_CHANGED)
                addAction(Intent.ACTION_BATTERY_LOW)
                addAction(Intent.ACTION_BATTERY_OKAY)
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
            }
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(BatteryTriggerReceiver(), filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    context.registerReceiver(BatteryTriggerReceiver(), filter)
                }
                Log.d(TAG, "Battery trigger receiver registered")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to register battery trigger receiver", e)
            }
        }
        
        /**
         * 获取当前电池状态
         */
        fun getCurrentBatteryState(context: Context): BatteryState {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val isCharging = batteryManager.isCharging
            
            return BatteryState(
                level = level,
                isCharging = isCharging
            )
        }
    }
    
    data class BatteryState(
        val level: Int,
        val isCharging: Boolean
    )
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Battery intent received: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED,
            Intent.ACTION_BATTERY_LOW,
            Intent.ACTION_BATTERY_OKAY,
            Intent.ACTION_POWER_CONNECTED,
            Intent.ACTION_POWER_DISCONNECTED -> {
                handleBatteryChange(context, intent)
            }
        }
    }
    
    private fun handleBatteryChange(context: Context, intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        
        val batteryPercent = if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            -1
        }
        
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        
        val isPowerConnected = intent.action == Intent.ACTION_POWER_CONNECTED ||
                isCharging
        
        Log.d(TAG, "Battery: $batteryPercent%, charging: $isCharging, powerConnected: $isPowerConnected")
        
        // 通知 MacroService 处理电池触发器
        MacroService.handleBatteryTrigger(context, batteryPercent, isCharging, isPowerConnected)
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