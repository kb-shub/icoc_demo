package com.example.icocdemo.models

data class BPMachine(
    val macId: String,
    val deviceName: String,
    var systolic: Int?,
    var diastolic: Int?,
    var pulse: Int?
)