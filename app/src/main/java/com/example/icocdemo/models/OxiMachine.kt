package com.example.icocdemo.models

data class OxiMachine(
    val macId: String,
    val deviceName: String,
    var spO2: Int?,
    var pulse: Int?,
    var isConnected: Boolean = true
)