package com.example.macrodroid.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
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

        setupClickListeners()
        
        // If editing, directly show the corresponding dialog
        editingTrigger?.let { trigger ->
            showEditDialogForTrigger(trigger)
        }
    }
    
    private fun showEditDialogForTrigger(trigger: Trigger) {
        when (trigger) {
            is Trigger.TimeTrigger -> showTimePicker()
            is Trigger.BatteryTrigger -> showBatteryTriggerDialog()
            is Trigger.ScreenTrigger -> showScreenTriggerDialog()
            is Trigger.SmsTrigger -> showSmsTriggerDialog()
            is Trigger.LocationTrigger -> {
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
                finish()
            }
            is Trigger.WiFiTrigger -> {
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
                finish()
            }
            is Trigger.BluetoothTrigger -> {
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun setupClickListeners() {
        // Time Trigger Card
        binding.btnTimeTrigger.setOnClickListener {
            showTimePicker()
        }

        // Battery Trigger Card
        binding.btnBatteryTrigger.setOnClickListener {
            showBatteryTriggerDialog()
        }

        // Location Trigger Card
        binding.btnLocationTrigger.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

        // WiFi Trigger Card
        binding.btnWiFiTrigger.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

        // Bluetooth Trigger Card
        binding.btnBluetoothTrigger.setOnClickListener {
            Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
        }

        // Screen Trigger Card
        binding.btnScreenTrigger.setOnClickListener {
            showScreenTriggerDialog()
        }

        // SMS Trigger Card
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
        
        timePicker.addOnNegativeButtonClickListener {
            if (editingTrigger != null) finish()
        }
        
        timePicker.addOnCancelListener {
            if (editingTrigger != null) finish()
        }

        timePicker.show(supportFragmentManager, "time_picker")
    }
    
    /**
     * 显示电池触发器配置对话框
     */
    private fun showBatteryTriggerDialog() {
        val existingTrigger = editingTrigger as? Trigger.BatteryTrigger
        
        val options = arrayOf(
            getString(R.string.battery_above_level),    // 电量高于
            getString(R.string.battery_below_level),    // 电量低于
            getString(R.string.battery_charging),       // 开始充电
            getString(R.string.battery_discharging)     // 停止充电
        )
        
        val currentType = existingTrigger?.type ?: Trigger.BatteryType.BELOW
        val checkedItem = when (currentType) {
            Trigger.BatteryType.ABOVE -> 0
            Trigger.BatteryType.BELOW -> 1
            Trigger.BatteryType.CHARGING -> 2
            Trigger.BatteryType.DISCHARGING -> 3
        }
        
        var selectedType = currentType
        var selectedLevel = existingTrigger?.level ?: 20
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.battery_trigger)
        
        // 创建自定义布局
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 16)
        }
        
        // 类型选择
        val typeTextView = TextView(this).apply {
            text = getString(R.string.trigger_condition)
            setPadding(0, 0, 0, 16)
            textSize = 16f
        }
        layout.addView(typeTextView)
        
        // 电量滑块（仅在选择高于/低于时显示）
        val levelContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 16, 0, 0)
        }
        
        val levelTextView = TextView(this).apply {
            text = getString(R.string.battery_level_format, selectedLevel)
            textSize = 16f
        }
        
        val seekBar = SeekBar(this).apply {
            max = 100
            progress = selectedLevel
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    selectedLevel = progress
                    levelTextView.text = getString(R.string.battery_level_format, progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        
        levelContainer.addView(levelTextView)
        levelContainer.addView(seekBar)
        
        layout.addView(levelContainer)
        
        // 设置初始显示状态
        levelContainer.visibility = if (currentType == Trigger.BatteryType.CHARGING || 
                                         currentType == Trigger.BatteryType.DISCHARGING) {
            View.GONE
        } else {
            View.VISIBLE
        }
        
        builder.setSingleChoiceItems(options, checkedItem) { _, which ->
            selectedType = when (which) {
                0 -> Trigger.BatteryType.ABOVE
                1 -> Trigger.BatteryType.BELOW
                2 -> Trigger.BatteryType.CHARGING
                3 -> Trigger.BatteryType.DISCHARGING
                else -> Trigger.BatteryType.BELOW
            }
            
            // 显示或隐藏电量滑块
            levelContainer.visibility = if (selectedType == Trigger.BatteryType.CHARGING || 
                                             selectedType == Trigger.BatteryType.DISCHARGING) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
        
        builder.setView(layout)
        
        builder.setPositiveButton(R.string.create) { _, _ ->
            val trigger = Trigger.BatteryTrigger(
                id = existingTrigger?.id ?: "battery_${System.currentTimeMillis()}",
                level = selectedLevel,
                type = selectedType
            )
            returnTrigger(trigger)
        }
        
        builder.setNegativeButton(R.string.cancel) { _, _ ->
            if (editingTrigger != null) finish()
        }
        builder.setOnCancelListener {
            if (editingTrigger != null) finish()
        }
        builder.show()
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
            .setNegativeButton(R.string.cancel) { _, _ ->
                if (editingTrigger != null) finish()
            }
            .setOnCancelListener {
                if (editingTrigger != null) finish()
            }
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

        builder.setNegativeButton(R.string.cancel) { _, _ ->
            if (editingTrigger != null) finish()
        }
        builder.setOnCancelListener {
            if (editingTrigger != null) finish()
        }
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