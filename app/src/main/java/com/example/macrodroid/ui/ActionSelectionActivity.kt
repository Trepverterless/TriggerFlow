package com.example.macrodroid.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.macrodroid.R
import com.example.macrodroid.data.Action
import com.example.macrodroid.databinding.ActivityActionSelectionBinding

class ActionSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityActionSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActionSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNotification.setOnClickListener {
            showNotificationDialog()
        }

        binding.btnWiFi.setOnClickListener {
            showWiFiActionDialog()
        }

        binding.btnBluetooth.setOnClickListener {
            showBluetoothActionDialog()
        }

        binding.btnVolume.setOnClickListener {
            showVolumeDialog()
        }

        binding.btnBrightness.setOnClickListener {
            showBrightnessDialog()
        }

        binding.btnVibration.setOnClickListener {
            val action = Action.VibrationAction(
                id = "vibration_${System.currentTimeMillis()}",
                pattern = Action.VibrationPattern.SHORT
            )
            returnAction(action)
        }

        binding.btnAlertSound.setOnClickListener {
            showAlertSoundDialog()
        }

        binding.btnLaunchApp.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.btnScreen.setOnClickListener {
            showScreenActionDialog()
        }

        binding.btnData.setOnClickListener {
            showDataActionDialog()
        }

        binding.btnScreenshot.setOnClickListener {
            val action = Action.ScreenshotAction(
                id = "screenshot_${System.currentTimeMillis()}"
            )
            returnAction(action)
        }

        binding.btnSendSMS.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotificationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.create_notification)

        val layout = layoutInflater.inflate(R.layout.dialog_notification, null)
        builder.setView(layout)

        builder.setPositiveButton(R.string.create) { _, _ ->
            val etTitle = layout.findViewById<EditText>(R.id.etTitle)
            val etContent = layout.findViewById<EditText>(R.id.etContent)

            val action = Action.NotificationAction(
                id = "notification_${System.currentTimeMillis()}",
                title = etTitle.text.toString(),
                content = etContent.text.toString()
            )
            returnAction(action)
        }

        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun showWiFiActionDialog() {
        val options = arrayOf(getString(R.string.enable_wifi), getString(R.string.disable_wifi), getString(R.string.toggle_wifi))
        AlertDialog.Builder(this)
            .setTitle(R.string.wifi_action)
            .setItems(options) { _, which ->
                val type = when (which) {
                    0 -> Action.WiFiActionType.ENABLE
                    1 -> Action.WiFiActionType.DISABLE
                    2 -> Action.WiFiActionType.TOGGLE
                    else -> Action.WiFiActionType.ENABLE
                }
                val action = Action.WiFiAction(
                    id = "wifi_${System.currentTimeMillis()}",
                    type = type
                )
                returnAction(action)
            }
            .show()
    }

    private fun showBluetoothActionDialog() {
        val options = arrayOf(getString(R.string.enable_bluetooth), getString(R.string.disable_bluetooth), getString(R.string.toggle_bluetooth))
        AlertDialog.Builder(this)
            .setTitle(R.string.bluetooth_action)
            .setItems(options) { _, which ->
                val type = when (which) {
                    0 -> Action.BluetoothActionType.ENABLE
                    1 -> Action.BluetoothActionType.DISABLE
                    2 -> Action.BluetoothActionType.TOGGLE
                    else -> Action.BluetoothActionType.ENABLE
                }
                val action = Action.BluetoothAction(
                    id = "bluetooth_${System.currentTimeMillis()}",
                    type = type
                )
                returnAction(action)
            }
            .show()
    }

    private fun showVolumeDialog() {
        val options = arrayOf(getString(R.string.ringtone), getString(R.string.media), getString(R.string.alarm), getString(R.string.notification_volume))
        AlertDialog.Builder(this)
            .setTitle(R.string.select_volume_type)
            .setItems(options) { _, which ->
                val volumeType = when (which) {
                    0 -> Action.VolumeType.RINGTONE
                    1 -> Action.VolumeType.MEDIA
                    2 -> Action.VolumeType.ALARM
                    3 -> Action.VolumeType.NOTIFICATION
                    else -> Action.VolumeType.RINGTONE
                }
                showVolumeLevelDialog(volumeType)
            }
            .show()
    }

    private fun showVolumeLevelDialog(volumeType: Action.VolumeType) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.set_volume_level)

        val layout = layoutInflater.inflate(R.layout.dialog_volume, null)
        builder.setView(layout)

        builder.setPositiveButton(R.string.set) { _, _ ->
            val etLevel = layout.findViewById<EditText>(R.id.etLevel)
            val level = etLevel.text.toString().toIntOrNull() ?: 50

            val action = Action.VolumeAction(
                id = "volume_${System.currentTimeMillis()}",
                volumeType = volumeType,
                level = level
            )
            returnAction(action)
        }

        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun showBrightnessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.set_brightness)

        val layout = layoutInflater.inflate(R.layout.dialog_brightness, null)
        builder.setView(layout)

        builder.setPositiveButton(R.string.set) { _, _ ->
            val etLevel = layout.findViewById<EditText>(R.id.etLevel)
            val level = etLevel.text.toString().toIntOrNull() ?: 50

            val action = Action.BrightnessAction(
                id = "brightness_${System.currentTimeMillis()}",
                level = level
            )
            returnAction(action)
        }

        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun showScreenActionDialog() {
        val options = arrayOf(getString(R.string.turn_on_screen), getString(R.string.turn_off_screen), getString(R.string.lock_screen), getString(R.string.unlock_screen))
        AlertDialog.Builder(this)
            .setTitle(R.string.screen_action)
            .setItems(options) { _, which ->
                val type = when (which) {
                    0 -> Action.ScreenActionType.ON
                    1 -> Action.ScreenActionType.OFF
                    2 -> Action.ScreenActionType.LOCK
                    3 -> Action.ScreenActionType.UNLOCK
                    else -> Action.ScreenActionType.ON
                }
                val action = Action.ScreenAction(
                    id = "screen_${System.currentTimeMillis()}",
                    type = type
                )
                returnAction(action)
            }
            .show()
    }

    private fun showDataActionDialog() {
        val options = arrayOf(getString(R.string.enable_data), getString(R.string.disable_data), getString(R.string.toggle_data))
        AlertDialog.Builder(this)
            .setTitle(R.string.data_action)
            .setItems(options) { _, which ->
                val type = when (which) {
                    0 -> Action.DataActionType.ENABLE
                    1 -> Action.DataActionType.DISABLE
                    2 -> Action.DataActionType.TOGGLE
                    else -> Action.DataActionType.ENABLE
                }
                val action = Action.DataAction(
                    id = "data_${System.currentTimeMillis()}",
                    type = type
                )
                returnAction(action)
            }
            .show()
    }

    private fun showAlertSoundDialog() {
        val options = arrayOf(
            getString(R.string.alert_sound_alarm),
            getString(R.string.alert_sound_notification),
            getString(R.string.alert_sound_ringtone),
            getString(R.string.alert_sound_default)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.select_alert_sound_type)
            .setItems(options) { _, which ->
                val soundType = when (which) {
                    0 -> Action.AlertSoundType.ALARM
                    1 -> Action.AlertSoundType.NOTIFICATION
                    2 -> Action.AlertSoundType.RINGTONE
                    3 -> Action.AlertSoundType.DEFAULT
                    else -> Action.AlertSoundType.DEFAULT
                }
                val action = Action.AlertSoundAction(
                    id = "alert_sound_${System.currentTimeMillis()}",
                    soundType = soundType
                )
                returnAction(action)
            }
            .show()
    }

    private fun returnAction(action: Action) {
        val intent = Intent().apply {
            putExtra("action", action)
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}