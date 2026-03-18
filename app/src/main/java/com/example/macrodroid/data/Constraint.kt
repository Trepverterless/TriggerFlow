package com.example.macrodroid.data

sealed class Constraint {
    data class TimeConstraint(
        val id: String,
        val startTime: String,
        val endTime: String,
        val days: List<Int>
    ) : Constraint()

    data class DayConstraint(
        val id: String,
        val days: List<Int>
    ) : Constraint()

    data class LocationConstraint(
        val id: String,
        val latitude: Double,
        val longitude: Double,
        val radius: Float,
        val locationName: String
    ) : Constraint()

    data class WiFiConstraint(
        val id: String,
        val ssid: String
    ) : Constraint()

    data class PowerConstraint(
        val id: String,
        val type: PowerType
    ) : Constraint()

    enum class PowerType {
        CHARGING, NOT_CHARGING, BATTERY_LOW, BATTERY_OK
    }
}