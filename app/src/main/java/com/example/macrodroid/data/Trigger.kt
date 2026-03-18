package com.example.macrodroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class Trigger : Parcelable {
    abstract val id: String

    @Parcelize
    data class TimeTrigger(
        override val id: String,
        val hour: Int,
        val minute: Int,
        val days: List<Int>
    ) : Trigger()

    @Parcelize
    data class LocationTrigger(
        override val id: String,
        val latitude: Double,
        val longitude: Double,
        val radius: Float,
        val locationName: String
    ) : Trigger()

    @Parcelize
    data class BatteryTrigger(
        override val id: String,
        val level: Int,
        val type: BatteryType
    ) : Trigger()

    @Parcelize
    data class WiFiTrigger(
        override val id: String,
        val ssid: String,
        val type: WiFiType
    ) : Trigger()

    @Parcelize
    data class BluetoothTrigger(
        override val id: String,
        val deviceName: String,
        val type: BluetoothType
    ) : Trigger()

    @Parcelize
    data class ScreenTrigger(
        override val id: String,
        val type: ScreenType
    ) : Trigger()

    @Parcelize
    data class SmsTrigger(
        override val id: String,
        val phoneNumber: String,
        val contentKeyword: String
    ) : Trigger()

    enum class BatteryType {
        ABOVE, BELOW, CHARGING, DISCHARGING
    }

    enum class WiFiType {
        CONNECTED, DISCONNECTED
    }

    enum class BluetoothType {
        CONNECTED, DISCONNECTED
    }

    enum class ScreenType {
        ON, OFF, UNLOCK
    }
}