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
    private var editingAction: Action? = null
    private var editPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActionSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if we're editing an existing action
        editingAction = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("edit_action", Action::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("edit_action")
        }
        editPosition = intent.getIntExtra("edit_position", -1)

        setupClickListeners()
        
        // If editing, directly show the corresponding dialog
        editingAction?.let { action ->
            showEditDialogForAction(action)
        }
    }
    
    private fun showEditDialogForAction(action: Action) {
        when (action) {
            is Action.NotificationAction -> showNotificationDialog()
            is Action.WiFiAction -> showWiFiActionDialog()
            is Action.BluetoothAction -> showBluetoothActionDialog()
            is Action.VolumeAction -> showVolumeDialog()
            is Action.BrightnessAction -> showBrightnessDialog()
            is Action.VibrationAction -> {
                // Vibration doesn't have a dialog, just return the action
                returnAction(action)
            }
            is Action.AlertSoundAction -> showAlertSoundDialog()
            is Action.ScreenAction -> showScreenActionDialog()
            is Action.DataAction -> showDataActionDialog()
            is Action.ScreenshotAction -> {
                // Screenshot doesn't have a dialog, just return the action
                returnAction(action)
            }
            is Action.LaunchAppAction -> {
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
                finish()
            }
            is Action.SendSMSAction -> {
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupClickListeners() {
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
            val existingAction = editingAction as? Action.VibrationAction
            val action = Action.VibrationAction(
                id = existingAction?.id ?: "vibration_${System.currentTimeMillis()}",
                pattern = existingAction?.pattern ?: Action.VibrationPattern.SHORT
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
            val existingAction = editingAction as? Action.ScreenshotAction
            val action = Action.ScreenshotAction(
                id = existingAction?.id ?: "screenshot_${System.currentTimeMillis()}"
            )
            returnAction(action)
        }

        binding.btnSendSMS.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotificationDialog() {
        val existingAction = editingAction as? Action.NotificationAction
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.create_notification)

        val layout = layoutInflater.inflate(R.layout.dialog_notification, null)
        val etTitle = layout.findViewById<EditText>(R.id.etTitle)
        val etContent = layout.findViewById<EditText>(R.id.etContent)
        
        // Pre-fill with existing values if editing
        existingAction?.let {
            etTitle.setText(it.title)
            etContent.setText(it.content)
        }
        
        builder.setView(layout)

        builder.setPositiveButton(R.string.create) { _, _ ->
            val action = Action.NotificationAction(
                id = existingAction?.id ?: "notification_${System.currentTimeMillis()}",
                title = etTitle.text.toString(),
                content = etContent.text.toString()
            )
            returnAction(action)
        }

        builder.setNegativeButton(R.string.cancel) { _, _ ->
            if (editingAction != null) finish()
        }
        builder.setOnCancelListener {
            if (editingAction != null) finish()
        }
        builder.show()
    }

    private fun showWiFiActionDialog() {
        val existingAction = editingAction as? Action.WiFiAction
        val currentType = existingAction?.type ?: Action.WiFiActionType.ENABLE
        val checkedItem = when (currentType) {
            Action.WiFiActionType.ENABLE -> 0
            Action.WiFiActionType.DISABLE -> 1
            Action.WiFiActionType.TOGGLE -> 2
        }
        
        val options = arrayOf(getString(R.string.enable_wifi), getString(R.string.disable_wifi), getString(R.string.toggle_wifi))
        AlertDialog.Builder(this)
            .setTitle(R.string.wifi_action)
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val type = when (which) {
                    0 -> Action.WiFiActionType.ENABLE
                    1 -> Action.WiFiActionType.DISABLE
                    2 -> Action.WiFiActionType.TOGGLE
                    else -> Action.WiFiActionType.ENABLE
                }
                val action = Action.WiFiAction(
                    id = existingAction?.id ?: "wifi_${System.currentTimeMillis()}",
                    type = type
                )
                dialog.dismiss()
                returnAction(action)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                if (editingAction != null) finish()
            }
            .setOnCancelListener {
                if (editingAction != null) finish()
            }
            .show()
    }

    private fun showBluetoothActionDialog() {
        val existingAction = editingAction as? Action.BluetoothAction
        val currentType = existingAction?.type ?: Action.BluetoothActionType.ENABLE
        val checkedItem = when (currentType) {
            Action.BluetoothActionType.ENABLE -> 0
            Action.BluetoothActionType.DISABLE -> 1
            Action.BluetoothActionType.TOGGLE -> 2
        }
        
        val options = arrayOf(getString(R.string.enable_bluetooth), getString(R.string.disable_bluetooth), getString(R.string.toggle_bluetooth))
        AlertDialog.Builder(this)
            .setTitle(R.string.bluetooth_action)
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val type = when (which) {
                    0 -> Action.BluetoothActionType.ENABLE
                    1 -> Action.BluetoothActionType.DISABLE
                    2 -> Action.BluetoothActionType.TOGGLE
                    else -> Action.BluetoothActionType.ENABLE
                }
                val action = Action.BluetoothAction(
                    id = existingAction?.id ?: "bluetooth_${System.currentTimeMillis()}",
                    type = type
                )
                dialog.dismiss()
                returnAction(action)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                if (editingAction != null) finish()
            }
            .setOnCancelListener {
                if (editingAction != null) finish()
            }
            .show()
    }

    private fun showVolumeDialog() {
        val existingAction = editingAction as? Action.VolumeAction
        val currentVolumeType = existingAction?.volumeType ?: Action.VolumeType.RINGTONE
        val checkedItem = when (currentVolumeType) {
            Action.VolumeType.RINGTONE -> 0
            Action.VolumeType.MEDIA -> 1
            Action.VolumeType.ALARM -> 2
            Action.VolumeType.NOTIFICATION -> 3
        }
        
        val options = arrayOf(getString(R.string.ringtone), getString(R.string.media), getString(R.string.alarm), getString(R.string.notification_volume))
        AlertDialog.Builder(this)
            .setTitle(R.string.select_volume_type)
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val volumeType = when (which) {
                    0 -> Action.VolumeType.RINGTONE
                    1 -> Action.VolumeType.MEDIA
                    2 -> Action.VolumeType.ALARM
                    3 -> Action.VolumeType.NOTIFICATION
                    else -> Action.VolumeType.RINGTONE
                }
                dialog.dismiss()
                showVolumeLevelDialog(volumeType)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                if (editingAction != null) finish()
            }
            .setOnCancelListener {
                if (editingAction != null) finish()
            }
            .show()
    }

    private fun showVolumeLevelDialog(volumeType: Action.VolumeType) {
        val existingAction = editingAction as? Action.VolumeAction
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.set_volume_level)

        val layout = layoutInflater.inflate(R.layout.dialog_volume, null)
        val etLevel = layout.findViewById<EditText>(R.id.etLevel)
        
        // Pre-fill with existing value
        existingAction?.let {
            etLevel.setText(it.level.toString())
        }
        
        builder.setView(layout)

        builder.setPositiveButton(R.string.set) { _, _ ->
            val level = etLevel.text.toString().toIntOrNull() ?: 50

            val action = Action.VolumeAction(
                id = existingAction?.id ?: "volume_${System.currentTimeMillis()}",
                volumeType = volumeType,
                level = level
            )
            returnAction(action)
        }

        builder.setNegativeButton(R.string.cancel) { _, _ ->
            if (editingAction != null) finish()
        }
        builder.setOnCancelListener {
            if (editingAction != null) finish()
        }
        builder.show()
    }

    private fun showBrightnessDialog() {
        val existingAction = editingAction as? Action.BrightnessAction
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.set_brightness)

        val layout = layoutInflater.inflate(R.layout.dialog_brightness, null)
        val etLevel = layout.findViewById<EditText>(R.id.etLevel)
        
        // Pre-fill with existing value
        existingAction?.let {
            etLevel.setText(it.level.toString())
        }
        
        builder.setView(layout)

        builder.setPositiveButton(R.string.set) { _, _ ->
            val level = etLevel.text.toString().toIntOrNull() ?: 50

            val action = Action.BrightnessAction(
                id = existingAction?.id ?: "brightness_${System.currentTimeMillis()}",
                level = level
            )
            returnAction(action)
        }

        builder.setNegativeButton(R.string.cancel) { _, _ ->
            if (editingAction != null) finish()
        }
        builder.setOnCancelListener {
            if (editingAction != null) finish()
        }
        builder.show()
    }

    private fun showScreenActionDialog() {
        val existingAction = editingAction as? Action.ScreenAction
        val currentType = existingAction?.type ?: Action.ScreenActionType.ON
        val checkedItem = when (currentType) {
            Action.ScreenActionType.ON -> 0
            Action.ScreenActionType.OFF -> 1
            Action.ScreenActionType.LOCK -> 2
            Action.ScreenActionType.UNLOCK -> 3
        }
        
        val options = arrayOf(getString(R.string.turn_on_screen), getString(R.string.turn_off_screen), getString(R.string.lock_screen), getString(R.string.unlock_screen))
        AlertDialog.Builder(this)
            .setTitle(R.string.screen_action)
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val type = when (which) {
                    0 -> Action.ScreenActionType.ON
                    1 -> Action.ScreenActionType.OFF
                    2 -> Action.ScreenActionType.LOCK
                    3 -> Action.ScreenActionType.UNLOCK
                    else -> Action.ScreenActionType.ON
                }
                val action = Action.ScreenAction(
                    id = existingAction?.id ?: "screen_${System.currentTimeMillis()}",
                    type = type
                )
                dialog.dismiss()
                returnAction(action)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                if (editingAction != null) finish()
            }
            .setOnCancelListener {
                if (editingAction != null) finish()
            }
            .show()
    }

    private fun showDataActionDialog() {
        val existingAction = editingAction as? Action.DataAction
        val currentType = existingAction?.type ?: Action.DataActionType.ENABLE
        val checkedItem = when (currentType) {
            Action.DataActionType.ENABLE -> 0
            Action.DataActionType.DISABLE -> 1
            Action.DataActionType.TOGGLE -> 2
        }
        
        val options = arrayOf(getString(R.string.enable_data), getString(R.string.disable_data), getString(R.string.toggle_data))
        AlertDialog.Builder(this)
            .setTitle(R.string.data_action)
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val type = when (which) {
                    0 -> Action.DataActionType.ENABLE
                    1 -> Action.DataActionType.DISABLE
                    2 -> Action.DataActionType.TOGGLE
                    else -> Action.DataActionType.ENABLE
                }
                val action = Action.DataAction(
                    id = existingAction?.id ?: "data_${System.currentTimeMillis()}",
                    type = type
                )
                dialog.dismiss()
                returnAction(action)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                if (editingAction != null) finish()
            }
            .setOnCancelListener {
                if (editingAction != null) finish()
            }
            .show()
    }

    private fun showAlertSoundDialog() {
        val existingAction = editingAction as? Action.AlertSoundAction
        val currentType = existingAction?.soundType ?: Action.AlertSoundType.DEFAULT
        val checkedItem = when (currentType) {
            Action.AlertSoundType.ALARM -> 0
            Action.AlertSoundType.NOTIFICATION -> 1
            Action.AlertSoundType.RINGTONE -> 2
            Action.AlertSoundType.DEFAULT -> 3
        }
        
        val options = arrayOf(
            getString(R.string.alert_sound_alarm),
            getString(R.string.alert_sound_notification),
            getString(R.string.alert_sound_ringtone),
            getString(R.string.alert_sound_default)
        )
        AlertDialog.Builder(this)
            .setTitle(R.string.select_alert_sound_type)
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val soundType = when (which) {
                    0 -> Action.AlertSoundType.ALARM
                    1 -> Action.AlertSoundType.NOTIFICATION
                    2 -> Action.AlertSoundType.RINGTONE
                    3 -> Action.AlertSoundType.DEFAULT
                    else -> Action.AlertSoundType.DEFAULT
                }
                val action = Action.AlertSoundAction(
                    id = existingAction?.id ?: "alert_sound_${System.currentTimeMillis()}",
                    soundType = soundType
                )
                dialog.dismiss()
                returnAction(action)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                if (editingAction != null) finish()
            }
            .setOnCancelListener {
                if (editingAction != null) finish()
            }
            .show()
    }

    private fun returnAction(action: Action) {
        val intent = Intent().apply {
            putExtra("action", action)
            if (editPosition >= 0) {
                putExtra("edit_position", editPosition)
            }
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}