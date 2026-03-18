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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = MacroRepository.getInstance(this)

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
                // 记录已显示过对话框
                prefs.edit().putBoolean(KEY_XIAOMI_DIALOG_SHOWN, true).apply()
            }
        }
    }
    
    private fun showXiaomiPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("小米设备权限设置")
            .setMessage("在小米设备上，应用需要额外的权限才能正常工作：\n\n" +
                       "1. 自启动权限：允许应用在后台运行\n" +
                       "2. 后台弹出界面权限：允许应用显示界面\n" +
                       "3. 省电策略：设置为无限制\n" +
                       "4. 震动权限：允许应用震动\n\n" +
                       "是否前往设置？")
            .setPositiveButton("前往设置") { _, _ ->
                openXiaomiPermissionSettings()
            }
            .setNegativeButton("稍后设置", null)
            .show()
    }
    
    private fun openXiaomiPermissionSettings() {
        try {
            // 尝试打开小米安全中心的应用权限设置
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
            
            // 提示用户需要开启的权限
            Toast.makeText(this, 
                "请在权限管理中开启：自启动、后台弹出界面、震动，并在省电策略中选择无限制",
                Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // 如果无法打开，提示用户手动设置
            Toast.makeText(this, 
                "无法自动打开设置，请手动前往：设置 -> 应用管理 -> ${getString(R.string.app_name)} -> 权限", 
                Toast.LENGTH_LONG).show()
        }
    }

    private fun loadMacros() {
        lifecycleScope.launch {
            repository.macros.collect { macros ->
                adapter.updateMacros(macros)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_xiaomi_guide -> {
                startActivity(Intent(this, XiaomiPermissionGuideActivity::class.java))
                return true
            }
            R.id.action_settings -> return true
            R.id.action_help -> return true
        }
        return super.onOptionsItemSelected(item)
    }
}