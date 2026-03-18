package com.example.macrodroid.data

data class Macro(
    val id: Int,
    val name: String,
    val description: String,
    val isEnabled: Boolean,
    val triggers: List<Trigger>,
    val actions: List<Action>,
    val constraints: List<Constraint>
)