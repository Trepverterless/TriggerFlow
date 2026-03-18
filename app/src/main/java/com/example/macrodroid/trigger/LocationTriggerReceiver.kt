package com.example.macrodroid.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import com.example.macrodroid.data.Trigger
import com.example.macrodroid.domain.MacroService

class LocationTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val locationName = intent.getStringExtra("locationName") ?: ""
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        val radius = intent.getFloatExtra("radius", 100f)

        val trigger = Trigger.LocationTrigger(
            id = intent.getStringExtra("triggerId") ?: "",
            latitude = latitude,
            longitude = longitude,
            radius = radius,
            locationName = locationName
        )

        MacroService.handleTrigger(context, trigger)
    }
}

class WiFiTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo.ssid?.removeSurrounding("\"")

            val trigger = Trigger.WiFiTrigger(
                id = intent.getStringExtra("triggerId") ?: "",
                ssid = ssid ?: "",
                type = if (wifiManager.isWifiEnabled) Trigger.WiFiType.CONNECTED else Trigger.WiFiType.DISCONNECTED
            )

            MacroService.handleTrigger(context, trigger)
        }
    }
}