package com.example.macrodroid.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.macrodroid.R
import com.example.macrodroid.action.ActionExecutor
import com.example.macrodroid.data.Action
import com.example.macrodroid.data.Constraint
import com.example.macrodroid.data.Macro
import com.example.macrodroid.data.MacroRepository
import com.example.macrodroid.data.Trigger
import com.example.macrodroid.data.TriggerLog
import com.example.macrodroid.data.TriggerLogRepository
import com.example.macrodroid.trigger.TimeTriggerReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar

class MacroService : Service() {
    private lateinit var repository: MacroRepository
    private lateinit var actionExecutor: ActionExecutor
    private lateinit var logRepository: TriggerLogRepository
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "macro_service_channel"
        private const val TAG = "MacroService"

        fun startService(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, MacroService::class.java))
            } else {
                context.startService(Intent(context, MacroService::class.java))
            }
        }

        fun handleTrigger(context: Context, trigger: Trigger) {
            val intent = Intent(context, MacroService::class.java).apply {
                action = "HANDLE_TRIGGER"
                putExtra("trigger_type", when (trigger) {
                    is Trigger.TimeTrigger -> "time"
                    is Trigger.LocationTrigger -> "location"
                    is Trigger.BatteryTrigger -> "battery"
                    is Trigger.WiFiTrigger -> "wifi"
                    is Trigger.BluetoothTrigger -> "bluetooth"
                    is Trigger.ScreenTrigger -> "screen"
                    is Trigger.SmsTrigger -> "sms"
                })
                putExtra("trigger_config", trigger.toString())
            }
            context.startService(intent)
        }

        fun handleSmsTrigger(context: Context, phoneNumber: String, messageBody: String) {
            val intent = Intent(context, MacroService::class.java).apply {
                action = "HANDLE_SMS_TRIGGER"
                putExtra("phone_number", phoneNumber)
                putExtra("message_body", messageBody)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        repository = MacroRepository.getInstance(this)
        actionExecutor = ActionExecutor(this)
        logRepository = TriggerLogRepository.getInstance(this)
        createNotificationChannel()
        startForegroundWithType()
        setupTriggers()
    }

    private fun startForegroundWithType() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "HANDLE_TRIGGER" -> {
                val triggerType = intent.getStringExtra("trigger_type")
                val triggerConfig = intent.getStringExtra("trigger_config")
                if (triggerType != null && triggerConfig != null) {
                    handleTrigger(triggerType, triggerConfig)
                }
            }
            "HANDLE_SMS_TRIGGER" -> {
                val phoneNumber = intent.getStringExtra("phone_number")
                val messageBody = intent.getStringExtra("message_body")
                if (phoneNumber != null && messageBody != null) {
                    handleSmsTrigger(phoneNumber, messageBody)
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.service_channel),
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.service_running))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun setupTriggers() {
        serviceScope.launch {
            repository.macros.collect { macros ->
                TimeTriggerReceiver.cancelAllTriggers(this@MacroService)

                macros.filter { it.isEnabled }.forEach { macro ->
                    macro.triggers.forEach { trigger ->
                        when (trigger) {
                            is Trigger.TimeTrigger -> {
                                TimeTriggerReceiver.scheduleTrigger(this@MacroService, trigger)
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun handleTrigger(triggerType: String, triggerConfig: String) {
        serviceScope.launch {
            val macros = repository.macros.value
            val triggeredMacros = macros.filter { macro ->
                macro.isEnabled && macro.triggers.any { trigger ->
                    matchesTrigger(trigger, triggerType, triggerConfig)
                } && checkConstraints(macro)
            }

            triggeredMacros.forEach { macro ->
                val eventName = getTriggerEventName(triggerType)
                val actionDescriptions = macro.actions.map { getActionDescription(it) }
                
                logRepository.addLog(
                    TriggerLog(
                        macroId = macro.id,
                        macroName = macro.name,
                        event = eventName,
                        actions = actionDescriptions
                    )
                )
                
                macro.actions.forEach { action ->
                    actionExecutor.executeAction(action)
                }
            }
        }
    }

    private fun getTriggerEventName(triggerType: String): String {
        return when (triggerType) {
            "time" -> "时间触发器"
            "location" -> "位置触发器"
            "battery" -> "电池触发器"
            "wifi" -> "WiFi触发器"
            "bluetooth" -> "蓝牙触发器"
            "screen" -> "屏幕触发器"
            "sms" -> "短信触发器"
            else -> "未知触发器"
        }
    }

    private fun matchesTrigger(trigger: Trigger, triggerType: String, triggerConfig: String): Boolean {
        return when (trigger) {
            is Trigger.TimeTrigger -> triggerType == "time" && triggerConfig.contains(trigger.id)
            is Trigger.LocationTrigger -> triggerType == "location" && triggerConfig.contains(trigger.id)
            is Trigger.BatteryTrigger -> triggerType == "battery" && triggerConfig.contains(trigger.id)
            is Trigger.WiFiTrigger -> triggerType == "wifi" && triggerConfig.contains(trigger.id)
            is Trigger.BluetoothTrigger -> triggerType == "bluetooth" && triggerConfig.contains(trigger.id)
            is Trigger.ScreenTrigger -> triggerType == "screen" && triggerConfig.contains(trigger.id)
            is Trigger.SmsTrigger -> triggerType == "sms" && triggerConfig.contains(trigger.id)
        }
    }

    private fun handleSmsTrigger(phoneNumber: String, messageBody: String) {
        serviceScope.launch {
            try {
                val macros = repository.macros.value
                val triggeredMacros = macros.filter { macro ->
                    macro.isEnabled && macro.triggers.any { trigger ->
                        matchesSmsTrigger(trigger, phoneNumber, messageBody)
                    } && checkConstraints(macro)
                }

                triggeredMacros.forEach { macro ->
                    val actionDescriptions = macro.actions.map { getActionDescription(it) }
                    
                    logRepository.addLog(
                        TriggerLog(
                            macroId = macro.id,
                            macroName = macro.name,
                            event = "短信触发器 (来自: $phoneNumber)",
                            actions = actionDescriptions
                        )
                    )

                    macro.actions.forEach { action ->
                        actionExecutor.executeAction(action)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleSmsTrigger", e)
            }
        }
    }

    private fun matchesSmsTrigger(trigger: Trigger, phoneNumber: String, messageBody: String): Boolean {
        return when (trigger) {
            is Trigger.SmsTrigger -> {
                // 处理电话号码匹配（支持+86前缀）
                val normalizedTriggerPhone = trigger.phoneNumber.replace("+86", "").replace("-", "").replace(" ", "")
                val normalizedActualPhone = phoneNumber.replace("+86", "").replace("-", "").replace(" ", "")
                
                val numberMatch = trigger.phoneNumber.isEmpty() || 
                    phoneNumber.contains(trigger.phoneNumber) || 
                    trigger.phoneNumber == phoneNumber ||
                    normalizedTriggerPhone == normalizedActualPhone ||
                    normalizedActualPhone.contains(normalizedTriggerPhone)
                
                // 处理内容匹配
                val contentMatch = trigger.contentKeyword.isEmpty() || 
                    messageBody.contains(trigger.contentKeyword, ignoreCase = true)

                numberMatch && contentMatch
            }
            else -> false
        }
    }

    private fun checkConstraints(macro: Macro): Boolean {
        // 如果没有约束条件，直接返回true
        if (macro.constraints.isEmpty()) {
            return true
        }

        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val currentTime = "${currentHour.toString().padStart(2, '0')}:${currentMinute.toString().padStart(2, '0')}"

        return macro.constraints.all { constraint ->
            when (constraint) {
                is Constraint.TimeConstraint -> {
                    val timeOk = currentTime >= constraint.startTime && currentTime <= constraint.endTime
                    val dayOk = constraint.days.contains(currentDay)
                    timeOk && dayOk
                }
                is Constraint.DayConstraint -> {
                    constraint.days.contains(currentDay)
                }
                is Constraint.LocationConstraint -> {
                    true
                }
                is Constraint.WiFiConstraint -> {
                    val wifiManager = getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    val currentSsid = wifiInfo.ssid?.removeSurrounding("\"") ?: ""
                    currentSsid == constraint.ssid
                }
                is Constraint.PowerConstraint -> {
                    val batteryStatus = getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
                    val isCharging = batteryStatus.isCharging
                    val level = batteryStatus.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)

                    when (constraint.type) {
                        Constraint.PowerType.CHARGING -> isCharging
                        Constraint.PowerType.NOT_CHARGING -> !isCharging
                        Constraint.PowerType.BATTERY_LOW -> level < 20
                        Constraint.PowerType.BATTERY_OK -> level >= 20
                    }
                }
            }
        }
    }

    private fun getActionDescription(action: Action): String {
        return when (action) {
            is Action.NotificationAction -> "通知: ${action.title.ifEmpty { "无标题" }}"
            is Action.WiFiAction -> "WiFi: ${when (action.type) {
                Action.WiFiActionType.ENABLE -> "启用"
                Action.WiFiActionType.DISABLE -> "禁用"
                Action.WiFiActionType.TOGGLE -> "切换"
            }}"
            is Action.BluetoothAction -> "蓝牙: ${when (action.type) {
                Action.BluetoothActionType.ENABLE -> "启用"
                Action.BluetoothActionType.DISABLE -> "禁用"
                Action.BluetoothActionType.TOGGLE -> "切换"
            }}"
            is Action.VolumeAction -> "音量: ${action.volumeType.name} ${action.level}%"
            is Action.BrightnessAction -> "亮度: ${action.level}%"
            is Action.VibrationAction -> "震动: ${when (action.pattern) {
                Action.VibrationPattern.SHORT -> "短震动"
                Action.VibrationPattern.MEDIUM -> "中震动"
                Action.VibrationPattern.LONG -> "长震动"
                Action.VibrationPattern.CUSTOM -> "自定义震动"
            }}"
            is Action.LaunchAppAction -> "启动应用: ${action.packageName.ifEmpty { "未选择" }}"
            is Action.ScreenAction -> "屏幕: ${when (action.type) {
                Action.ScreenActionType.ON -> "打开"
                Action.ScreenActionType.OFF -> "关闭"
                Action.ScreenActionType.LOCK -> "锁定"
                Action.ScreenActionType.UNLOCK -> "解锁"
            }}"
            is Action.DataAction -> "数据: ${when (action.type) {
                Action.DataActionType.ENABLE -> "启用"
                Action.DataActionType.DISABLE -> "禁用"
                Action.DataActionType.TOGGLE -> "切换"
            }}"
            is Action.ScreenshotAction -> "截图"
            is Action.SendSMSAction -> "发送短信: ${action.phoneNumber}"
            is Action.AlertSoundAction -> "提示音: ${when (action.soundType) {
                Action.AlertSoundType.ALARM -> "闹钟"
                Action.AlertSoundType.NOTIFICATION -> "通知"
                Action.AlertSoundType.RINGTONE -> "铃声"
                Action.AlertSoundType.DEFAULT -> "默认"
            }}"
        }
    }
}