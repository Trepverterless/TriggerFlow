package com.example.macrodroid.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class MacroConfig(
    val id: Int,
    val name: String,
    val description: String,
    val isEnabled: Boolean,
    val triggers: List<TriggerData>,
    val actions: List<ActionData>,
    val constraints: List<ConstraintData>
)

@Serializable
data class TriggerData(
    val type: String,
    val config: Map<String, String>
)

@Serializable
data class ActionData(
    val type: String,
    val config: Map<String, String>
)

@Serializable
data class ConstraintData(
    val type: String,
    val config: Map<String, String>
)

class MacroRepository private constructor(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("macro_prefs", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _macros = MutableStateFlow<List<Macro>>(emptyList())
    val macros: StateFlow<List<Macro>> = _macros

    init {
        loadMacros()
    }

    companion object {
        @Volatile
        private var instance: MacroRepository? = null

        fun getInstance(context: Context): MacroRepository {
            return instance ?: synchronized(this) {
                instance ?: MacroRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private fun loadMacros() {
        val macrosJson = sharedPreferences.getString("macros", null)
        if (macrosJson != null) {
            try {
                val macroConfigs = json.decodeFromString<List<MacroConfig>>(macrosJson)
                _macros.value = macroConfigs.map { config ->
                    Macro(
                        id = config.id,
                        name = config.name,
                        description = config.description,
                        isEnabled = config.isEnabled,
                        triggers = config.triggers.map { parseTrigger(it) },
                        actions = config.actions.map { parseAction(it) },
                        constraints = config.constraints.map { parseConstraint(it) }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveMacros() {
        val macroConfigs = _macros.value.map { macro ->
            MacroConfig(
                id = macro.id,
                name = macro.name,
                description = macro.description,
                isEnabled = macro.isEnabled,
                triggers = macro.triggers.map { serializeTrigger(it) },
                actions = macro.actions.map { serializeAction(it) },
                constraints = macro.constraints.map { serializeConstraint(it) }
            )
        }
        val macrosJson = json.encodeToString(macroConfigs)
        sharedPreferences.edit().putString("macros", macrosJson).commit()
    }

    fun addMacro(macro: Macro) {
        _macros.value = _macros.value + macro
        saveMacros()
    }

    fun updateMacro(macro: Macro) {
        _macros.value = _macros.value.map { if (it.id == macro.id) macro else it }
        saveMacros()
    }

    fun deleteMacro(id: Int) {
        _macros.value = _macros.value.filter { it.id != id }
        saveMacros()
    }

    fun getMacroById(id: Int): Macro? {
        return _macros.value.find { it.id == id }
    }

    private fun parseTrigger(triggerData: TriggerData): Trigger {
        return when (triggerData.type) {
            "time" -> Trigger.TimeTrigger(
                id = triggerData.config["id"] ?: "",
                hour = triggerData.config["hour"]?.toIntOrNull() ?: 0,
                minute = triggerData.config["minute"]?.toIntOrNull() ?: 0,
                days = triggerData.config["days"]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            )
            "location" -> Trigger.LocationTrigger(
                id = triggerData.config["id"] ?: "",
                latitude = triggerData.config["latitude"]?.toDoubleOrNull() ?: 0.0,
                longitude = triggerData.config["longitude"]?.toDoubleOrNull() ?: 0.0,
                radius = triggerData.config["radius"]?.toFloatOrNull() ?: 100f,
                locationName = triggerData.config["locationName"] ?: ""
            )
            "battery" -> Trigger.BatteryTrigger(
                id = triggerData.config["id"] ?: "",
                level = triggerData.config["level"]?.toIntOrNull() ?: 0,
                type = when (triggerData.config["type"]) {
                    "above" -> Trigger.BatteryType.ABOVE
                    "below" -> Trigger.BatteryType.BELOW
                    "charging" -> Trigger.BatteryType.CHARGING
                    "discharging" -> Trigger.BatteryType.DISCHARGING
                    else -> Trigger.BatteryType.ABOVE
                }
            )
            "wifi" -> Trigger.WiFiTrigger(
                id = triggerData.config["id"] ?: "",
                ssid = triggerData.config["ssid"] ?: "",
                type = if (triggerData.config["type"] == "connected") Trigger.WiFiType.CONNECTED else Trigger.WiFiType.DISCONNECTED
            )
            "bluetooth" -> Trigger.BluetoothTrigger(
                id = triggerData.config["id"] ?: "",
                deviceName = triggerData.config["deviceName"] ?: "",
                type = if (triggerData.config["type"] == "connected") Trigger.BluetoothType.CONNECTED else Trigger.BluetoothType.DISCONNECTED
            )
            "screen" -> Trigger.ScreenTrigger(
                id = triggerData.config["id"] ?: "",
                type = when (triggerData.config["type"]) {
                    "on" -> Trigger.ScreenType.ON
                    "off" -> Trigger.ScreenType.OFF
                    "unlock" -> Trigger.ScreenType.UNLOCK
                    else -> Trigger.ScreenType.ON
                }
            )
            "sms" -> Trigger.SmsTrigger(
                id = triggerData.config["id"] ?: "",
                phoneNumber = triggerData.config["phoneNumber"] ?: "",
                contentKeyword = triggerData.config["contentKeyword"] ?: ""
            )
            else -> throw IllegalArgumentException("Unknown trigger type: ${triggerData.type}")
        }
    }

    private fun parseAction(actionData: ActionData): Action {
        return when (actionData.type) {
            "notification" -> Action.NotificationAction(
                id = actionData.config["id"] ?: "",
                title = actionData.config["title"] ?: "",
                content = actionData.config["content"] ?: ""
            )
            "wifi" -> Action.WiFiAction(
                id = actionData.config["id"] ?: "",
                type = when (actionData.config["type"]) {
                    "enable" -> Action.WiFiActionType.ENABLE
                    "disable" -> Action.WiFiActionType.DISABLE
                    "toggle" -> Action.WiFiActionType.TOGGLE
                    else -> Action.WiFiActionType.ENABLE
                }
            )
            "bluetooth" -> Action.BluetoothAction(
                id = actionData.config["id"] ?: "",
                type = when (actionData.config["type"]) {
                    "enable" -> Action.BluetoothActionType.ENABLE
                    "disable" -> Action.BluetoothActionType.DISABLE
                    "toggle" -> Action.BluetoothActionType.TOGGLE
                    else -> Action.BluetoothActionType.ENABLE
                }
            )
            "volume" -> Action.VolumeAction(
                id = actionData.config["id"] ?: "",
                volumeType = when (actionData.config["volumeType"]) {
                    "ringtone" -> Action.VolumeType.RINGTONE
                    "media" -> Action.VolumeType.MEDIA
                    "alarm" -> Action.VolumeType.ALARM
                    "notification" -> Action.VolumeType.NOTIFICATION
                    else -> Action.VolumeType.RINGTONE
                },
                level = actionData.config["level"]?.toIntOrNull() ?: 50
            )
            "brightness" -> Action.BrightnessAction(
                id = actionData.config["id"] ?: "",
                level = actionData.config["level"]?.toIntOrNull() ?: 50
            )
            "vibration" -> Action.VibrationAction(
                id = actionData.config["id"] ?: "",
                pattern = when (actionData.config["pattern"]) {
                    "short" -> Action.VibrationPattern.SHORT
                    "medium" -> Action.VibrationPattern.MEDIUM
                    "long" -> Action.VibrationPattern.LONG
                    "custom" -> Action.VibrationPattern.CUSTOM
                    else -> Action.VibrationPattern.SHORT
                }
            )
            "launch_app" -> Action.LaunchAppAction(
                id = actionData.config["id"] ?: "",
                packageName = actionData.config["packageName"] ?: "",
                activityName = actionData.config["activityName"] ?: ""
            )
            "screen" -> Action.ScreenAction(
                id = actionData.config["id"] ?: "",
                type = when (actionData.config["type"]) {
                    "on" -> Action.ScreenActionType.ON
                    "off" -> Action.ScreenActionType.OFF
                    "lock" -> Action.ScreenActionType.LOCK
                    "unlock" -> Action.ScreenActionType.UNLOCK
                    else -> Action.ScreenActionType.ON
                }
            )
            "data" -> Action.DataAction(
                id = actionData.config["id"] ?: "",
                type = when (actionData.config["type"]) {
                    "enable" -> Action.DataActionType.ENABLE
                    "disable" -> Action.DataActionType.DISABLE
                    "toggle" -> Action.DataActionType.TOGGLE
                    else -> Action.DataActionType.ENABLE
                }
            )
            "screenshot" -> Action.ScreenshotAction(
                id = actionData.config["id"] ?: ""
            )
            "sms" -> Action.SendSMSAction(
                id = actionData.config["id"] ?: "",
                phoneNumber = actionData.config["phoneNumber"] ?: "",
                message = actionData.config["message"] ?: ""
            )
            "alert_sound" -> Action.AlertSoundAction(
                id = actionData.config["id"] ?: "",
                soundType = when (actionData.config["soundType"]) {
                    "alarm" -> Action.AlertSoundType.ALARM
                    "notification" -> Action.AlertSoundType.NOTIFICATION
                    "ringtone" -> Action.AlertSoundType.RINGTONE
                    else -> Action.AlertSoundType.DEFAULT
                }
            )
            else -> throw IllegalArgumentException("Unknown action type: ${actionData.type}")
        }
    }

    private fun parseConstraint(constraintData: ConstraintData): Constraint {
        return when (constraintData.type) {
            "time" -> Constraint.TimeConstraint(
                id = constraintData.config["id"] ?: "",
                startTime = constraintData.config["startTime"] ?: "",
                endTime = constraintData.config["endTime"] ?: "",
                days = constraintData.config["days"]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            )
            "day" -> Constraint.DayConstraint(
                id = constraintData.config["id"] ?: "",
                days = constraintData.config["days"]?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
            )
            "location" -> Constraint.LocationConstraint(
                id = constraintData.config["id"] ?: "",
                latitude = constraintData.config["latitude"]?.toDoubleOrNull() ?: 0.0,
                longitude = constraintData.config["longitude"]?.toDoubleOrNull() ?: 0.0,
                radius = constraintData.config["radius"]?.toFloatOrNull() ?: 100f,
                locationName = constraintData.config["locationName"] ?: ""
            )
            "wifi" -> Constraint.WiFiConstraint(
                id = constraintData.config["id"] ?: "",
                ssid = constraintData.config["ssid"] ?: ""
            )
            "power" -> Constraint.PowerConstraint(
                id = constraintData.config["id"] ?: "",
                type = when (constraintData.config["type"]) {
                    "charging" -> Constraint.PowerType.CHARGING
                    "not_charging" -> Constraint.PowerType.NOT_CHARGING
                    "battery_low" -> Constraint.PowerType.BATTERY_LOW
                    "battery_ok" -> Constraint.PowerType.BATTERY_OK
                    else -> Constraint.PowerType.CHARGING
                }
            )
            else -> throw IllegalArgumentException("Unknown constraint type: ${constraintData.type}")
        }
    }

    private fun serializeTrigger(trigger: Trigger): TriggerData {
        val config = mutableMapOf<String, String>()
        val type: String

        when (trigger) {
            is Trigger.TimeTrigger -> {
                type = "time"
                config["id"] = trigger.id
                config["hour"] = trigger.hour.toString()
                config["minute"] = trigger.minute.toString()
                config["days"] = trigger.days.joinToString(",")
            }
            is Trigger.LocationTrigger -> {
                type = "location"
                config["id"] = trigger.id
                config["latitude"] = trigger.latitude.toString()
                config["longitude"] = trigger.longitude.toString()
                config["radius"] = trigger.radius.toString()
                config["locationName"] = trigger.locationName
            }
            is Trigger.BatteryTrigger -> {
                type = "battery"
                config["id"] = trigger.id
                config["level"] = trigger.level.toString()
                config["type"] = when (trigger.type) {
                    Trigger.BatteryType.ABOVE -> "above"
                    Trigger.BatteryType.BELOW -> "below"
                    Trigger.BatteryType.CHARGING -> "charging"
                    Trigger.BatteryType.DISCHARGING -> "discharging"
                }
            }
            is Trigger.WiFiTrigger -> {
                type = "wifi"
                config["id"] = trigger.id
                config["ssid"] = trigger.ssid
                config["type"] = if (trigger.type == Trigger.WiFiType.CONNECTED) "connected" else "disconnected"
            }
            is Trigger.BluetoothTrigger -> {
                type = "bluetooth"
                config["id"] = trigger.id
                config["deviceName"] = trigger.deviceName
                config["type"] = if (trigger.type == Trigger.BluetoothType.CONNECTED) "connected" else "disconnected"
            }
            is Trigger.ScreenTrigger -> {
                type = "screen"
                config["id"] = trigger.id
                config["type"] = when (trigger.type) {
                    Trigger.ScreenType.ON -> "on"
                    Trigger.ScreenType.OFF -> "off"
                    Trigger.ScreenType.UNLOCK -> "unlock"
                }
            }
            is Trigger.SmsTrigger -> {
                type = "sms"
                config["id"] = trigger.id
                config["phoneNumber"] = trigger.phoneNumber
                config["contentKeyword"] = trigger.contentKeyword
            }
        }

        return TriggerData(type, config)
    }

    private fun serializeAction(action: Action): ActionData {
        val config = mutableMapOf<String, String>()
        val type: String

        when (action) {
            is Action.NotificationAction -> {
                type = "notification"
                config["id"] = action.id
                config["title"] = action.title
                config["content"] = action.content
            }
            is Action.WiFiAction -> {
                type = "wifi"
                config["id"] = action.id
                config["type"] = when (action.type) {
                    Action.WiFiActionType.ENABLE -> "enable"
                    Action.WiFiActionType.DISABLE -> "disable"
                    Action.WiFiActionType.TOGGLE -> "toggle"
                }
            }
            is Action.BluetoothAction -> {
                type = "bluetooth"
                config["id"] = action.id
                config["type"] = when (action.type) {
                    Action.BluetoothActionType.ENABLE -> "enable"
                    Action.BluetoothActionType.DISABLE -> "disable"
                    Action.BluetoothActionType.TOGGLE -> "toggle"
                }
            }
            is Action.VolumeAction -> {
                type = "volume"
                config["id"] = action.id
                config["volumeType"] = when (action.volumeType) {
                    Action.VolumeType.RINGTONE -> "ringtone"
                    Action.VolumeType.MEDIA -> "media"
                    Action.VolumeType.ALARM -> "alarm"
                    Action.VolumeType.NOTIFICATION -> "notification"
                }
                config["level"] = action.level.toString()
            }
            is Action.BrightnessAction -> {
                type = "brightness"
                config["id"] = action.id
                config["level"] = action.level.toString()
            }
            is Action.VibrationAction -> {
                type = "vibration"
                config["id"] = action.id
                config["pattern"] = when (action.pattern) {
                    Action.VibrationPattern.SHORT -> "short"
                    Action.VibrationPattern.MEDIUM -> "medium"
                    Action.VibrationPattern.LONG -> "long"
                    Action.VibrationPattern.CUSTOM -> "custom"
                }
            }
            is Action.LaunchAppAction -> {
                type = "launch_app"
                config["id"] = action.id
                config["packageName"] = action.packageName
                config["activityName"] = action.activityName
            }
            is Action.ScreenAction -> {
                type = "screen"
                config["id"] = action.id
                config["type"] = when (action.type) {
                    Action.ScreenActionType.ON -> "on"
                    Action.ScreenActionType.OFF -> "off"
                    Action.ScreenActionType.LOCK -> "lock"
                    Action.ScreenActionType.UNLOCK -> "unlock"
                }
            }
            is Action.DataAction -> {
                type = "data"
                config["id"] = action.id
                config["type"] = when (action.type) {
                    Action.DataActionType.ENABLE -> "enable"
                    Action.DataActionType.DISABLE -> "disable"
                    Action.DataActionType.TOGGLE -> "toggle"
                }
            }
            is Action.ScreenshotAction -> {
                type = "screenshot"
                config["id"] = action.id
            }
            is Action.SendSMSAction -> {
                type = "sms"
                config["id"] = action.id
                config["phoneNumber"] = action.phoneNumber
                config["message"] = action.message
            }
            is Action.AlertSoundAction -> {
                type = "alert_sound"
                config["id"] = action.id
                config["soundType"] = when (action.soundType) {
                    Action.AlertSoundType.ALARM -> "alarm"
                    Action.AlertSoundType.NOTIFICATION -> "notification"
                    Action.AlertSoundType.RINGTONE -> "ringtone"
                    Action.AlertSoundType.DEFAULT -> "default"
                }
            }
        }

        return ActionData(type, config)
    }

    private fun serializeConstraint(constraint: Constraint): ConstraintData {
        val config = mutableMapOf<String, String>()
        val type: String

        when (constraint) {
            is Constraint.TimeConstraint -> {
                type = "time"
                config["id"] = constraint.id
                config["startTime"] = constraint.startTime
                config["endTime"] = constraint.endTime
                config["days"] = constraint.days.joinToString(",")
            }
            is Constraint.DayConstraint -> {
                type = "day"
                config["id"] = constraint.id
                config["days"] = constraint.days.joinToString(",")
            }
            is Constraint.LocationConstraint -> {
                type = "location"
                config["id"] = constraint.id
                config["latitude"] = constraint.latitude.toString()
                config["longitude"] = constraint.longitude.toString()
                config["radius"] = constraint.radius.toString()
                config["locationName"] = constraint.locationName
            }
            is Constraint.WiFiConstraint -> {
                type = "wifi"
                config["id"] = constraint.id
                config["ssid"] = constraint.ssid
            }
            is Constraint.PowerConstraint -> {
                type = "power"
                config["id"] = constraint.id
                config["type"] = when (constraint.type) {
                    Constraint.PowerType.CHARGING -> "charging"
                    Constraint.PowerType.NOT_CHARGING -> "not_charging"
                    Constraint.PowerType.BATTERY_LOW -> "battery_low"
                    Constraint.PowerType.BATTERY_OK -> "battery_ok"
                }
            }
        }

        return ConstraintData(type, config)
    }
}