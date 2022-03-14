package com.example.icocdemo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.icocdemo.models.BleDevice

class AddDevicesViewModel  : ViewModel() {
    val bleDevices = MutableLiveData<MutableList<BleDevice>>(mutableListOf())
}