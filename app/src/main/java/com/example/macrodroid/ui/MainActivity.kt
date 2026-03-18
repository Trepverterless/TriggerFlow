package com.example.macrodroid.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.macrodroid.R
import com.example.macrodroid.data.Macro
import com.example.macrodroid.data.MacroRepository
import com.example.macrodroid.databinding.ActivityMainBinding
import com.example.macrodroid.domain.MacroService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: MacroRepository
    private lateinit var adapter: MacroAdapter
    
    companion object {
        private const val REQUEST_SMS_PERMISSION = 100
        private const val REQUEST_NOTIFICATION_PERMISSION = 101
        private const val PREFS_NAME = "macro_prefs"
        private const val KEY_XIAOMI_DIALOG_SHOWN = "xiaomi_dialog_shown"
        private const val GITHUB_URL = "https://github.com/Trepverterless/TriggerFlow"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MacroRepository.getInstance(this)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        MacroService.startService(this)

        // 请求必要的权限
        requestNecessaryPermissions()

        adapter = MacroAdapter(
            macros = emptyList(),
            onItemClick = { macro ->
                val intent = Intent(this, MacroDetailActivity::class.java).apply {
                    putExtra("macro_id", macro.id)
                }
                startActivity(intent)
            },
            onToggle = { macro ->
                repository.updateMacro(macro)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            val newMacro = Macro(
                id = System.currentTimeMillis().toInt(),
                name = getString(R.string.new_macro),
                description = getString(R.string.description),
                isEnabled = true,
                triggers = emptyList(),
                actions = emptyList(),
                constraints = emptyList()
            )
            repository.addMacro(newMacro)
            val intent = Intent(this, MacroDetailActivity::class.java).apply {
                putExtra("macro_id", newMacro.id)
            }
            startActivity(intent)
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_macros -> true
                R.id.nav_logs -> {
                    startActivity(Intent(this, TriggerLogActivity::class.java))
                    false
                }
                else -> false
            }
        }

        loadMacros()
    }
    
    private fun requestNecessaryPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        // 短信权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECEIVE_SMS)
        }
        
        // 通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                REQUEST_SMS_PERMISSION
            )
        } else {
            // 权限已授予，检查小米特殊权限
            checkXiaomiSpecialPermissions()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SMS_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "短信权限已授权", Toast.LENGTH_SHORT).show()
                // 检查小米特殊权限
                checkXiaomiSpecialPermissions()
            } else {
                Toast.makeText(this, "短信权限被拒绝，短信触发器可能无法工作", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun checkXiaomiSpecialPermissions() {
        // 检测是否是小米设备
        val manufacturer = Build.MANUFACTURER.lowercase()
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi")) {
            // 检查是否已经显示过对话框
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val hasShownDialog = prefs.getBoolean(KEY_XIAOMI_DIALOG_SHOWN, false)
            
            if (!hasShownDialog) {
                showXiaomiPermissionDialog()
                prefs.edit().putBoolean(KEY_XIAOMI_DIALOG_SHOWN, true).apply()
            }
        }
    }
    
    private fun showXiaomiPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.xiaomi_permission_guide)
            .setMessage("检测到您使用的是小米设备，为确保应用正常运行，请在设置中开启以下权限：\n\n" +
                    "1. 自启动权限\n" +
                    "2. 后台弹出界面权限\n" +
                    "3. 省电策略选择无限制\n\n" +
                    "这些权限可以确保触发器在后台正常工作。")
            .setPositiveButton("前往设置") { dialog, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "无法打开设置", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("稍后提醒") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun loadMacros() {
        lifecycleScope.launch {
            repository.macros.collect { macros ->
                adapter.updateMacros(macros)
                
                // 显示或隐藏空状态视图
                if (macros.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_permission_guide -> {
                showPermissionGuide()
                true
            }
            R.id.action_help -> {
                showHelp()
                true
            }
            R.id.action_about -> {
                showAbout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showPermissionGuide() {
        val message = """
【应用权限说明】

为确保应用正常工作，需要以下权限：

▶ 必要权限
• 短信权限：用于短信触发器功能
• 通知权限：用于显示通知动作和服务状态

▶ 小米/红米设备额外设置
• 自启动权限：确保后台服务运行
• 后台弹出界面权限：允许后台交互
• 省电策略设为无限制：避免被系统杀掉

▶ 震动功能说明
• 需要在系统设置中开启触觉反馈
• 路径：设置 → 声音与震动 → 点按震动

▶ 如何授予权限
1. 打开系统设置
2. 找到应用管理 → TriggerFlow
3. 在权限管理中授予所需权限

如遇到问题，请检查以上设置是否正确配置。
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_guide)
            .setMessage(message)
            .setPositiveButton("打开应用设置") { dialog, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "无法打开设置", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showHelp() {
        val message = """
【使用帮助】

▶ 什么是宏？
宏是一组自动化规则的集合，包含触发器和动作。当触发条件满足时，自动执行预设的动作。

▶ 如何创建宏？
1. 点击右下角的+按钮
2. 输入宏名称和描述
3. 添加触发器（如何触发）
4. 添加动作（执行什么操作）
5. 点击保存

▶ 触发器类型
• 时间触发器：在指定时间触发
• 电池触发器：电量变化时触发
• 短信触发器：收到特定短信时触发
• 屏幕触发器：屏幕开关时触发
• 更多类型开发中...

▶ 动作类型
• 发送通知：显示自定义通知
• 控制WiFi/蓝牙/数据：开关或切换状态
• 调整音量/亮度：设置系统参数
• 振动/播放提示音：提醒功能
• 截图：自动截取屏幕
• 更多类型开发中...

▶ 注意事项
• 确保应用有必要的权限
• 小米设备需要额外设置自启动权限
• 某些功能可能需要无障碍服务支持

▶ 问题排查
1. 检查权限是否正确授予
2. 检查宏是否已启用
3. 查看触发记录了解执行情况
4. 重启应用或重新创建宏
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle(R.string.help_title)
            .setMessage(message)
            .setPositiveButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showAbout() {
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "1.0.0"
        }
        
        val message = """
${getString(R.string.about_description)}

${getString(R.string.about_features)}

${getString(R.string.about_version)}: $versionName

${getString(R.string.about_github)}:
$GITHUB_URL
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle(R.string.about_title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.visit_github)) { dialog, _ ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}