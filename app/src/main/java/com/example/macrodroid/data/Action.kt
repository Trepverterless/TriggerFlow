package com.example.macrodroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Action : Parcelable {
    @Parcelize
    data class NotificationAction(
        val id: String,
        val title: String,
        val content: String
    ) : Action()

    @Parcelize
    data class WiFiAction(
        val id: String,
        val type: WiFiActionType
    ) : Action()

    @Parcelize
    data class BluetoothAction(
        val id: String,
        val type: BluetoothActionType
    ) : Action()

    @Parcelize
    data class VolumeAction(
        val id: String,
        val volumeType: VolumeType,
        val level: Int
    ) : Action()

    @Parcelize
    data class BrightnessAction(
        val id: String,
        val level: Int
    ) : Action()

    @Parcelize
    data class VibrationAction(
        val id: String,
        val pattern: VibrationPattern
    ) : Action()

    @Parcelize
    data class LaunchAppAction(
        val id: String,
        val packageName: String,
        val activityName: String
    ) : Action()

    @Parcelize
    data class ScreenAction(
        val id: String,
        val type: ScreenActionType
    ) : Action()

    @Parcelize
    data class DataAction(
        val id: String,
        val type: DataActionType
    ) : Action()

    @Parcelize
    data class ScreenshotAction(
        val id: String
    ) : Action()

    @Parcelize
    data class SendSMSAction(
        val id: String,
        val phoneNumber: String,
        val message: String
    ) : Action()

    @Parcelize
    data class AlertSoundAction(
        val id: String,
        val soundType: AlertSoundType
    ) : Action()

    enum class WiFiActionType {
        ENABLE, DISABLE, TOGGLE
    }

    enum class BluetoothActionType {
        ENABLE, DISABLE, TOGGLE
    }

    enum class VolumeType {
        RINGTONE, MEDIA, ALARM, NOTIFICATION
    }

    enum class VibrationPattern {
        SHORT, MEDIUM, LONG, CUSTOM
    }

    enum class ScreenActionType {
        ON, OFF, LOCK, UNLOCK
    }

    enum class DataActionType {
        ENABLE, DISABLE, TOGGLE
    }

    enum class AlertSoundType {
        ALARM, NOTIFICATION, RINGTONE, DEFAULT
    }
}