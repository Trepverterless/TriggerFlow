package com.example.macrodroid.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.macrodroid.R
import com.example.macrodroid.data.Trigger
import com.example.macrodroid.databinding.ActivityTriggerSelectionBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat

class TriggerSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTriggerSelectionBinding
    private var editingTrigger: Trigger? = null
    private var editPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTriggerSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if we're editing an existing trigger
        editingTrigger = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("edit_trigger", Trigger::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("edit_trigger")
        }
        editPosition = intent.getIntExtra("edit_position", -1)

        binding.btnTimeTrigger.setOnClickListener {
            showTimePicker()
        }

        binding.btnLocationTrigger.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.btnBatteryTrigger.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.btnWiFiTrigger.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.btnBluetoothTrigger.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

        binding.btnScreenTrigger.setOnClickListener {
            showScreenTriggerDialog()
        }

        binding.btnSmsTrigger.setOnClickListener {
            showSmsTriggerDialog()
        }
    }

    private fun showTimePicker() {
        val existingTrigger = editingTrigger as? Trigger.TimeTrigger
        val initialHour = existingTrigger?.hour ?: 12
        val initialMinute = existingTrigger?.minute ?: 0

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(initialHour)
            .setMinute(initialMinute)
            .setTitleText(getString(R.string.select_time))
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val trigger = Trigger.TimeTrigger(
                id = existingTrigger?.id ?: "time_${System.currentTimeMillis()}",
                hour = timePicker.hour,
                minute = timePicker.minute,
                days = existingTrigger?.days ?: listOf(1, 2, 3, 4, 5)
            )
            returnTrigger(trigger)
        }

        timePicker.show(supportFragmentManager, "time_picker")
    }

    private fun showScreenTriggerDialog() {
        val existingTrigger = editingTrigger as? Trigger.ScreenTrigger
        val options = arrayOf(
            getString(R.string.turn_on_screen),
            getString(R.string.turn_off_screen),
            getString(R.string.unlock_screen)
        )
        val currentType = existingTrigger?.type ?: Trigger.ScreenType.ON
        val checkedItem = when (currentType) {
            Trigger.ScreenType.ON -> 0
            Trigger.ScreenType.OFF -> 1
            Trigger.ScreenType.UNLOCK -> 2
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.screen_trigger)
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val type = when (which) {
                    0 -> Trigger.ScreenType.ON
                    1 -> Trigger.ScreenType.OFF
                    2 -> Trigger.ScreenType.UNLOCK
                    else -> Trigger.ScreenType.ON
                }
                val trigger = Trigger.ScreenTrigger(
                    id = existingTrigger?.id ?: "screen_${System.currentTimeMillis()}",
                    type = type
                )
                dialog.dismiss()
                returnTrigger(trigger)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showSmsTriggerDialog() {
        val existingTrigger = editingTrigger as? Trigger.SmsTrigger

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.create_sms_trigger)
        builder.setMessage(R.string.sms_trigger_description)

        val layout = layoutInflater.inflate(R.layout.dialog_sms_trigger, null)
        val etPhoneNumber = layout.findViewById<EditText>(R.id.etPhoneNumber)
        val etContentKeyword = layout.findViewById<EditText>(R.id.etContentKeyword)

        // Pre-fill with existing values if editing
        existingTrigger?.let {
            etPhoneNumber.setText(it.phoneNumber)
            etContentKeyword.setText(it.contentKeyword)
        }

        builder.setView(layout)

        builder.setPositiveButton(R.string.create) { _, _ ->
            val phoneNumber = etPhoneNumber.text.toString().trim()
            val contentKeyword = etContentKeyword.text.toString().trim()

            val trigger = Trigger.SmsTrigger(
                id = existingTrigger?.id ?: "sms_${System.currentTimeMillis()}",
                phoneNumber = phoneNumber,
                contentKeyword = contentKeyword
            )
            returnTrigger(trigger)
        }

        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    private fun returnTrigger(trigger: Trigger) {
        val intent = Intent().apply {
            putExtra("trigger", trigger)
            if (editPosition >= 0) {
                putExtra("edit_position", editPosition)
            }
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}