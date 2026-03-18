package com.example.macrodroid.action

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.macrodroid.R
import com.example.macrodroid.data.Action

class ActionExecutor(private val context: Context) {
    
    companion object {
        private const val TAG = "ActionExecutor"
        
        // 检测是否是小米设备
        private fun isXiaomiDevice(): Boolean {
            val manufacturer = Build.MANUFACTURER.lowercase()
            return manufacturer.contains("xiaomi") || manufacturer.contains("redmi")
        }
    }

    fun executeAction(action: Action) {
        Log.d(TAG, "Executing action: ${action::class.simpleName}")
        when (action) {
            is Action.NotificationAction -> executeNotificationAction(action)
            is Action.WiFiAction -> executeWiFiAction(action)
            is Action.BluetoothAction -> executeBluetoothAction(action)
            is Action.VolumeAction -> executeVolumeAction(action)
            is Action.BrightnessAction -> executeBrightnessAction(action)
            is Action.VibrationAction -> executeVibrationAction(action)
            is Action.LaunchAppAction -> executeLaunchAppAction(action)
            is Action.ScreenAction -> executeScreenAction(action)
            is Action.DataAction -> executeDataAction(action)
            is Action.ScreenshotAction -> executeScreenshotAction(action)
            is Action.SendSMSAction -> executeSendSMSAction(action)
            is Action.AlertSoundAction -> executeAlertSoundAction(action)
        }
    }

    private fun executeNotificationAction(action: Action.NotificationAction) {
        val channelId = "macro_notification_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(action.title)
            .setContentText(action.content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(action.hashCode(), notification)
    }

    private fun executeWiFiAction(action: Action.WiFiAction) {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val isEnabled = wifiManager.isWifiEnabled

        when (action.type) {
            Action.WiFiActionType.ENABLE -> wifiManager.isWifiEnabled = true
            Action.WiFiActionType.DISABLE -> wifiManager.isWifiEnabled = false
            Action.WiFiActionType.TOGGLE -> wifiManager.isWifiEnabled = !isEnabled
        }
    }

    private fun executeBluetoothAction(action: Action.BluetoothAction) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter != null) {
            when (action.type) {
                Action.BluetoothActionType.ENABLE -> bluetoothAdapter.enable()
                Action.BluetoothActionType.DISABLE -> bluetoothAdapter.disable()
                Action.BluetoothActionType.TOGGLE -> {
                    if (bluetoothAdapter.isEnabled) {
                        bluetoothAdapter.disable()
                    } else {
                        bluetoothAdapter.enable()
                    }
                }
            }
        }
    }

    private fun executeVolumeAction(action: Action.VolumeAction) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val streamType = when (action.volumeType) {
            Action.VolumeType.RINGTONE -> AudioManager.STREAM_RING
            Action.VolumeType.MEDIA -> AudioManager.STREAM_MUSIC
            Action.VolumeType.ALARM -> AudioManager.STREAM_ALARM
            Action.VolumeType.NOTIFICATION -> AudioManager.STREAM_NOTIFICATION
        }

        val maxVolume = audioManager.getStreamMaxVolume(streamType)
        val targetVolume = (maxVolume * action.level / 100).toInt()

        audioManager.setStreamVolume(streamType, targetVolume, 0)
    }

    private fun executeBrightnessAction(action: Action.BrightnessAction) {
        try {
            val brightness = action.level / 255f
            android.provider.Settings.System.putInt(
                context.contentResolver,
                android.provider.Settings.System.SCREEN_BRIGHTNESS,
                (brightness * 255).toInt()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun executeVibrationAction(action: Action.VibrationAction) {
        Log.d(TAG, "executeVibrationAction: pattern=${action.pattern}, isXiaomi=${isXiaomiDevice()}")
        
        // 检查震动权限
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "VIBRATE permission not granted")
            return
        }
        
        try {
            // 获取 Vibrator 服务
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            // 检查设备是否支持震动
            if (!vibrator.hasVibrator()) {
                Log.w(TAG, "Device does not have a vibrator")
                return
            }

            val hasAmplitudeControl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.hasAmplitudeControl()
            } else {
                false
            }
            Log.d(TAG, "Device has vibrator: true, hasAmplitudeControl: $hasAmplitudeControl")

            // 在主线程执行震动
            Handler(Looper.getMainLooper()).post {
                try {
                    // 小米设备使用特殊的震动方式
                    if (isXiaomiDevice()) {
                        performXiaomiVibration(vibrator, action.pattern)
                    } else {
                        // 原生Android震动
                        performStandardVibration(vibrator, action.pattern)
                    }
                    
                    Log.d(TAG, "Vibration execution completed successfully")
                    
                } catch (e: SecurityException) {
                    Log.e(TAG, "SecurityException: Missing VIBRATE permission", e)
                } catch (e: Exception) {
                    Log.e(TAG, "Error executing vibration in handler", e)
                }
            }
            
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Missing VIBRATE permission", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing vibration", e)
        }
    }
    
    /**
     * 小米设备震动实现
     * 参考小米震动SDK文档：https://dev.mi.com/xiaomihyperos/documentation/detail?pId=2183
     * 
     * 小米/澎湃OS的震动特点：
     * 1. 支持预定义震动效果（API 29+）
     * 2. 波形震动需要使用合适的振幅
     * 3. 某些预定义效果在小米设备上有更好的反馈
     */
    private fun performXiaomiVibration(vibrator: Vibrator, pattern: Action.VibrationPattern) {
        Log.d(TAG, "Performing Xiaomi vibration: $pattern")
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (API 29) 支持预定义震动效果
                when (pattern) {
                    Action.VibrationPattern.SHORT -> {
                        // 短震动：1秒持续震动
                        vibrateForDuration(vibrator, 1000)
                        Log.d(TAG, "Xiaomi short vibration: 1 second continuous")
                    }
                    Action.VibrationPattern.MEDIUM -> {
                        // 中震动：1秒持续震动
                        vibrateForDuration(vibrator, 1000)
                        Log.d(TAG, "Xiaomi medium vibration: 1 second continuous")
                    }
                    Action.VibrationPattern.LONG -> {
                        // 长震动：1秒持续震动
                        vibrateForDuration(vibrator, 1000)
                        Log.d(TAG, "Xiaomi long vibration: 1 second continuous")
                    }
                    Action.VibrationPattern.CUSTOM -> {
                        // 自定义震动：1秒持续震动
                        vibrateForDuration(vibrator, 1000)
                        Log.d(TAG, "Xiaomi custom vibration: 1 second continuous")
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0-9 使用波形震动，持续1秒
                vibrateForDuration(vibrator, 1000)
                Log.d(TAG, "Xiaomi vibration (API 26-28): 1 second waveform")
            } else {
                // Android 8.0以下的小米设备
                @Suppress("DEPRECATION")
                vibrator.vibrate(1000)
                Log.d(TAG, "Xiaomi vibration (legacy): 1 second")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in Xiaomi vibration, falling back to standard", e)
            // 出错时回退到标准震动
            performStandardVibration(vibrator, pattern)
        }
    }
    
    /**
     * 持续震动指定时长
     */
    private fun vibrateForDuration(vibrator: Vibrator, durationMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 使用波形震动实现持续震动
            // 波形：立即开始震动，持续指定时长
            val timings = longArrayOf(0, durationMs)
            val amplitudes = intArrayOf(0, 255)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
            Log.d(TAG, "Vibrating for $durationMs ms with amplitude 255")
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
            Log.d(TAG, "Vibrating for $durationMs ms (legacy)")
        }
    }
    
    /**
     * 标准Android震动实现
     */
    private fun performStandardVibration(vibrator: Vibrator, pattern: Action.VibrationPattern) {
        Log.d(TAG, "Performing standard Android vibration: $pattern")
        
        // 所有震动模式都持续1秒
        vibrateForDuration(vibrator, 1000)
        Log.d(TAG, "Standard vibration: 1 second continuous")
    }

    private fun executeLaunchAppAction(action: Action.LaunchAppAction) {
        try {
            val intent = Intent().apply {
                setClassName(action.packageName, action.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun executeScreenAction(action: Action.ScreenAction) {
        when (action.type) {
            Action.ScreenActionType.ON -> {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                val wakeLock = powerManager.newWakeLock(
                    android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK or android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "macrodroid:wake_lock"
                ).apply {
                    acquire(10 * 60 * 1000L)
                }
                wakeLock.release()
            }
            Action.ScreenActionType.OFF, Action.ScreenActionType.LOCK -> {
                val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                devicePolicyManager.lockNow()
            }
            Action.ScreenActionType.UNLOCK -> {
                // Unlocking requires device admin or accessibility service
            }
        }
    }

    private fun executeDataAction(action: Action.DataAction) {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            @Suppress("DEPRECATION")
            val isEnabled = connectivityManager.getNetworkInfo(android.net.ConnectivityManager.TYPE_MOBILE)?.isConnected ?: false

            // MOBILE_DATA is a hidden API constant, use string literal instead
            when (action.type) {
                Action.DataActionType.ENABLE -> {
                    android.provider.Settings.Global.putInt(
                        context.contentResolver,
                        "mobile_data",
                        1
                    )
                }
                Action.DataActionType.DISABLE -> {
                    android.provider.Settings.Global.putInt(
                        context.contentResolver,
                        "mobile_data",
                        0
                    )
                }
                Action.DataActionType.TOGGLE -> {
                    android.provider.Settings.Global.putInt(
                        context.contentResolver,
                        "mobile_data",
                        if (isEnabled) 0 else 1
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun executeScreenshotAction(action: Action.ScreenshotAction) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val screenshotIntent = Intent("android.intent.action.SCREENSHOT")
                context.sendBroadcast(screenshotIntent)
            }, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun executeSendSMSAction(action: Action.SendSMSAction) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("smsto:${action.phoneNumber}")
                putExtra("sms_body", action.message)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun executeAlertSoundAction(action: Action.AlertSoundAction) {
        try {
            val ringtoneUri: Uri = when (action.soundType) {
                Action.AlertSoundType.ALARM -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                Action.AlertSoundType.NOTIFICATION -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                Action.AlertSoundType.RINGTONE -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                Action.AlertSoundType.DEFAULT -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            
            val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
            ringtone.play()
            
            Log.d(TAG, "Alert sound started: ${action.soundType}")
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alert sound", e)
        }
    }
}