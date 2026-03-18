package com.example.macrodroid.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.macrodroid.R
import com.example.macrodroid.data.Action
import com.example.macrodroid.data.Macro
import com.example.macrodroid.data.MacroRepository
import com.example.macrodroid.data.Trigger
import com.example.macrodroid.databinding.ActivityMacroDetailBinding
import kotlinx.coroutines.launch

class MacroDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMacroDetailBinding
    private lateinit var repository: MacroRepository
    private var macroId: Int = -1
    private var currentMacro: Macro? = null
    private lateinit var triggerAdapter: TriggerItemAdapter
    private lateinit var actionAdapter: ActionItemAdapter
    
    // 保存用户输入的临时状态
    private var isUserEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMacroDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MacroRepository.getInstance(this)
        macroId = intent.getIntExtra("macro_id", -1)

        if (macroId == -1) {
            finish()
            return
        }

        setupRecyclerViews()

        binding.btnSave.setOnClickListener {
            saveMacro()
        }

        binding.btnAddTrigger.setOnClickListener {
            saveUserInput()
            val intent = Intent(this, TriggerSelectionActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_TRIGGER)
        }

        binding.btnAddAction.setOnClickListener {
            saveUserInput()
            val intent = Intent(this, ActionSelectionActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ACTION)
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmDialog()
        }

        // 监听用户输入
        binding.etName.setOnFocusChangeListener { _, hasFocus ->
            isUserEditing = hasFocus
        }
        binding.etDescription.setOnFocusChangeListener { _, hasFocus ->
            isUserEditing = hasFocus
        }

        // Load initial data immediately
        loadMacroOnce()
        
        // Then observe for changes
        observeMacroChanges()
    }
    
    private fun saveUserInput() {
        currentMacro?.let { macro ->
            val name = binding.etName.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            currentMacro = macro.copy(
                name = name.ifEmpty { macro.name },
                description = description
            )
        }
    }

    private fun setupRecyclerViews() {
        triggerAdapter = TriggerItemAdapter(
            onItemClick = { position ->
                editTrigger(position)
            },
            onDeleteClick = { position ->
                deleteTrigger(position)
            }
        )
        binding.recyclerTriggers.layoutManager = LinearLayoutManager(this)
        binding.recyclerTriggers.adapter = triggerAdapter

        actionAdapter = ActionItemAdapter(
            onItemClick = { position ->
                editAction(position)
            },
            onDeleteClick = { position ->
                deleteAction(position)
            }
        )
        binding.recyclerActions.layoutManager = LinearLayoutManager(this)
        binding.recyclerActions.adapter = actionAdapter
    }

    private fun showDeleteConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.confirm_delete)
            .setMessage(R.string.delete_message)
            .setPositiveButton(R.string.delete_macro) { _, _ ->
                deleteMacro()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteMacro() {
        currentMacro?.let { macro ->
            repository.deleteMacro(macro.id)
            finish()
        }
    }

    private fun loadMacroOnce() {
        val macro = repository.macros.value.find { it.id == macroId }
        if (macro != null) {
            currentMacro = macro
            updateUI(macro, true)
        } else {
            finish()
        }
    }

    private fun observeMacroChanges() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                repository.macros.collect { macros ->
                    val macro = macros.find { it.id == macroId }
                    if (macro != null) {
                        currentMacro = macro
                        // 只在不是用户正在编辑时更新UI
                        if (!isUserEditing) {
                            updateUI(macro, false)
                        } else {
                            // 只更新列表，不更新EditText
                            triggerAdapter.updateItems(macro.triggers)
                            actionAdapter.updateItems(macro.actions)
                            updateEmptyViews(macro)
                        }
                    } else if (currentMacro != null) {
                        finish()
                    }
                }
            }
        }
    }

    private fun updateUI(macro: Macro, isInitial: Boolean) {
        if (isInitial) {
            binding.etName.setText(macro.name)
            binding.etDescription.setText(macro.description)
        }
        binding.swEnabled.isChecked = macro.isEnabled
        triggerAdapter.updateItems(macro.triggers)
        actionAdapter.updateItems(macro.actions)
        updateEmptyViews(macro)
    }

    private fun updateEmptyViews(macro: Macro) {
        binding.tvNoTriggers.visibility = if (macro.triggers.isEmpty()) View.VISIBLE else View.GONE
        binding.tvNoActions.visibility = if (macro.actions.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun saveMacro() {
        val name = binding.etName.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val isEnabled = binding.swEnabled.isChecked

        if (name.isEmpty()) {
            binding.etName.error = getString(R.string.enter_name)
            return
        }

        currentMacro?.let { macro ->
            val updatedMacro = macro.copy(
                name = name,
                description = description,
                isEnabled = isEnabled
            )
            repository.updateMacro(updatedMacro)
            finish()
        }
    }

    private fun editTrigger(position: Int) {
        saveUserInput()
        currentMacro?.let { macro ->
            val trigger = macro.triggers.getOrNull(position) ?: return
            val intent = Intent(this, TriggerSelectionActivity::class.java).apply {
                putExtra("edit_trigger", trigger)
                putExtra("edit_position", position)
            }
            startActivityForResult(intent, REQUEST_CODE_EDIT_TRIGGER)
        }
    }

    private fun deleteTrigger(position: Int) {
        currentMacro?.let { macro ->
            val updatedTriggers = macro.triggers.toMutableList()
            updatedTriggers.removeAt(position)
            val updatedMacro = macro.copy(triggers = updatedTriggers)
            repository.updateMacro(updatedMacro)
        }
    }

    private fun editAction(position: Int) {
        saveUserInput()
        currentMacro?.let { macro ->
            val action = macro.actions.getOrNull(position) ?: return
            val intent = Intent(this, ActionSelectionActivity::class.java).apply {
                putExtra("edit_action", action)
                putExtra("edit_position", position)
            }
            startActivityForResult(intent, REQUEST_CODE_EDIT_ACTION)
        }
    }

    private fun deleteAction(position: Int) {
        currentMacro?.let { macro ->
            val updatedActions = macro.actions.toMutableList()
            updatedActions.removeAt(position)
            val updatedMacro = macro.copy(actions = updatedActions)
            repository.updateMacro(updatedMacro)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CODE_TRIGGER -> {
                    val trigger = getTriggerFromIntent(data)
                    trigger?.let { addTrigger(it) }
                }
                REQUEST_CODE_EDIT_TRIGGER -> {
                    val trigger = getTriggerFromIntent(data)
                    val position = data.getIntExtra("edit_position", -1)
                    if (trigger != null && position >= 0) {
                        updateTrigger(position, trigger)
                    }
                }
                REQUEST_CODE_ACTION -> {
                    val action = getActionFromIntent(data)
                    action?.let { addAction(it) }
                }
                REQUEST_CODE_EDIT_ACTION -> {
                    val action = getActionFromIntent(data)
                    val position = data.getIntExtra("edit_position", -1)
                    if (action != null && position >= 0) {
                        updateAction(position, action)
                    }
                }
            }
        }
    }

    private fun getTriggerFromIntent(data: Intent): Trigger? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            data.getParcelableExtra("trigger", Trigger::class.java)
        } else {
            @Suppress("DEPRECATION")
            data.getParcelableExtra("trigger")
        }
    }

    private fun getActionFromIntent(data: Intent): Action? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            data.getParcelableExtra("action", Action::class.java)
        } else {
            @Suppress("DEPRECATION")
            data.getParcelableExtra("action")
        }
    }

    private fun addTrigger(trigger: Trigger) {
        currentMacro?.let { macro ->
            val updatedMacro = macro.copy(triggers = macro.triggers + trigger)
            repository.updateMacro(updatedMacro)
        }
    }

    private fun updateTrigger(position: Int, trigger: Trigger) {
        currentMacro?.let { macro ->
            val updatedTriggers = macro.triggers.toMutableList()
            if (position in updatedTriggers.indices) {
                updatedTriggers[position] = trigger
                val updatedMacro = macro.copy(triggers = updatedTriggers)
                repository.updateMacro(updatedMacro)
            }
        }
    }

    private fun addAction(action: Action) {
        currentMacro?.let { macro ->
            val updatedMacro = macro.copy(actions = macro.actions + action)
            repository.updateMacro(updatedMacro)
        }
    }

    private fun updateAction(position: Int, action: Action) {
        currentMacro?.let { macro ->
            val updatedActions = macro.actions.toMutableList()
            if (position in updatedActions.indices) {
                updatedActions[position] = action
                val updatedMacro = macro.copy(actions = updatedActions)
                repository.updateMacro(updatedMacro)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_TRIGGER = 1
        private const val REQUEST_CODE_ACTION = 2
        private const val REQUEST_CODE_EDIT_TRIGGER = 3
        private const val REQUEST_CODE_EDIT_ACTION = 4

        fun getTriggerDescription(trigger: Trigger): String {
            return when (trigger) {
                is Trigger.TimeTrigger -> "时间: ${String.format("%02d:%02d", trigger.hour, trigger.minute)}"
                is Trigger.LocationTrigger -> "位置: ${trigger.locationName.ifEmpty { "未命名" }}"
                is Trigger.BatteryTrigger -> "电池: ${trigger.type.name} ${trigger.level}%"
                is Trigger.WiFiTrigger -> "WiFi: ${if (trigger.type == Trigger.WiFiType.CONNECTED) "连接" else "断开"} ${trigger.ssid.ifEmpty { "任意" }}"
                is Trigger.BluetoothTrigger -> "蓝牙: ${if (trigger.type == Trigger.BluetoothType.CONNECTED) "连接" else "断开"} ${trigger.deviceName.ifEmpty { "任意设备" }}"
                is Trigger.ScreenTrigger -> "屏幕: ${when (trigger.type) {
                    Trigger.ScreenType.ON -> "打开"
                    Trigger.ScreenType.OFF -> "关闭"
                    Trigger.ScreenType.UNLOCK -> "解锁"
                }}"
                is Trigger.SmsTrigger -> {
                    val number = if (trigger.phoneNumber.isEmpty()) "任意号码" else trigger.phoneNumber
                    val keyword = if (trigger.contentKeyword.isEmpty()) "任意内容" else trigger.contentKeyword
                    "短信: $number 包含\"$keyword\""
                }
            }
        }

        fun getActionDescription(action: Action): String {
            return when (action) {
                is Action.NotificationAction -> "通知: ${action.title.ifEmpty { "无标题" }}"
                is Action.WiFiAction -> "WiFi: ${when (action.type) {
                    Action.WiFiActionType.ENABLE -> "启用"
                    Action.WiFiActionType.DISABLE -> "禁用"
                    Action.WiFiActionType.TOGGLE -> "切换"
                }}"
                is Action.BluetoothAction -> "蓝牙: ${when (action.type) {
                    Action.BluetoothActionType.ENABLE -> "启用"
                    Action.BluetoothActionType.DISABLE -> "禁用"
                    Action.BluetoothActionType.TOGGLE -> "切换"
                }}"
                is Action.VolumeAction -> "音量: ${action.volumeType.name} ${action.level}%"
                is Action.BrightnessAction -> "亮度: ${action.level}%"
                is Action.VibrationAction -> "振动: ${action.pattern.name}"
                is Action.LaunchAppAction -> "启动应用: ${action.packageName.ifEmpty { "未选择" }}"
                is Action.ScreenAction -> "屏幕: ${when (action.type) {
                    Action.ScreenActionType.ON -> "打开"
                    Action.ScreenActionType.OFF -> "关闭"
                    Action.ScreenActionType.LOCK -> "锁定"
                    Action.ScreenActionType.UNLOCK -> "解锁"
                }}"
                is Action.DataAction -> "数据: ${when (action.type) {
                    Action.DataActionType.ENABLE -> "启用"
                    Action.DataActionType.DISABLE -> "禁用"
                    Action.DataActionType.TOGGLE -> "切换"
                }}"
                is Action.ScreenshotAction -> "截图"
                is Action.SendSMSAction -> "发送短信: ${action.phoneNumber}"
                is Action.AlertSoundAction -> "提示音: ${when (action.soundType) {
                    Action.AlertSoundType.ALARM -> "闹钟"
                    Action.AlertSoundType.NOTIFICATION -> "通知"
                    Action.AlertSoundType.RINGTONE -> "铃声"
                    Action.AlertSoundType.DEFAULT -> "默认"
                }}"
            }
        }
    }
}

class TriggerItemAdapter(
    private val onItemClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<TriggerItemAdapter.ViewHolder>() {

    private var items: List<Trigger> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tvItemDescription)
        val btnDelete: View = view.findViewById(R.id.btnDeleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trigger_action, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trigger = items[position]
        holder.tvDescription.text = MacroDetailActivity.getTriggerDescription(trigger)
        holder.itemView.setOnClickListener { onItemClick(position) }
        holder.btnDelete.setOnClickListener { onDeleteClick(position) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Trigger>) {
        items = newItems
        notifyDataSetChanged()
    }
}

class ActionItemAdapter(
    private val onItemClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<ActionItemAdapter.ViewHolder>() {

    private var items: List<Action> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tvItemDescription)
        val btnDelete: View = view.findViewById(R.id.btnDeleteItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trigger_action, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action = items[position]
        holder.tvDescription.text = MacroDetailActivity.getActionDescription(action)
        holder.itemView.setOnClickListener { onItemClick(position) }
        holder.btnDelete.setOnClickListener { onDeleteClick(position) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Action>) {
        items = newItems
        notifyDataSetChanged()
    }
}