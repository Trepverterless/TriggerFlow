package com.example.macrodroid.data

data class TriggerLog(
    val id: Long = System.currentTimeMillis(),
    val macroId: Int,
    val macroName: String,
    val event: String,
    val actions: List<String> = emptyList(), // 触发的动作列表
    val timestamp: Long = System.currentTimeMillis()
)