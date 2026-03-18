package com.example.macrodroid.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.macrodroid.R
import com.example.macrodroid.data.TriggerLogRepository
import com.example.macrodroid.databinding.ActivityTriggerLogBinding
import kotlinx.coroutines.launch

class TriggerLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTriggerLogBinding
    private lateinit var repository: TriggerLogRepository
    private lateinit var adapter: TriggerLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTriggerLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.trigger_log_title)

        repository = TriggerLogRepository.getInstance(this)

        adapter = TriggerLogAdapter(emptyList(), repository)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.btnClear.setOnClickListener {
            showClearConfirmation()
        }

        binding.bottomNavigation.selectedItemId = R.id.nav_logs
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_macros -> {
                    finish()
                    true
                }
                R.id.nav_logs -> true
                else -> false
            }
        }

        loadLogs()
    }

    private fun loadLogs() {
        lifecycleScope.launch {
            repository.logs.collect { logs ->
                adapter.updateLogs(logs)
                binding.emptyView.visibility = if (logs.isEmpty()) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
            }
        }
    }

    private fun showClearConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.clear_log_title)
            .setMessage(R.string.clear_log_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                repository.clearLogs()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}