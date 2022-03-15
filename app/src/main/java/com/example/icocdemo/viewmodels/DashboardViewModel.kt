package com.example.icocdemo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.icocdemo.models.BPMachine
import com.example.icocdemo.models.OxiMachine
import com.example.icocdemo.models.WeighMachine

class DashboardViewModel : ViewModel() {
    val liveWeighMachines = MutableLiveData<MutableList<WeighMachine>>(mutableListOf())
    val liveBPMachines = MutableLiveData<MutableList<BPMachine>>(mutableListOf())
    val liveOxiMachines = MutableLiveData<MutableList<OxiMachine>>(mutableListOf())

    /**
     * Live data list filterer
     */
    fun setNewWeighDevices(
        listToLiveData: MutableList<WeighMachine>
    ) {
        var listSizeChange = false
        if (liveWeighMachines.value!!.isNotEmpty()) {
            liveWeighMachines.value?.forEach {
                val bleDevice = listToLiveData.find { liveDevice -> liveDevice.macId == it.macId }
                if (bleDevice == null) {
                    listSizeChange = true
                    listToLiveData.add(it)
                }
            }
        } else {
            listSizeChange = true
        }
        if (listSizeChange) {
            liveWeighMachines.value = listToLiveData
        }
    }

    fun setNewBPDevices(
        listToLiveData: MutableList<BPMachine>
    ) {
        var listSizeChange = false
        if (liveBPMachines.value!!.isNotEmpty()) {
            liveBPMachines.value?.forEach {
                val bleDevice = listToLiveData.find { liveDevice -> liveDevice.macId == it.macId }
                if (bleDevice == null) {
                    listSizeChange = true
                    listToLiveData.add(it)
                }
            }
        } else {
            listSizeChange = true
        }
        if (listSizeChange) {
            liveBPMachines.value = listToLiveData
        }
    }

    fun setNewOxiDevices(listToLiveData: MutableList<OxiMachine>) {
        var listSizeChange = false
        if (liveOxiMachines.value!!.isNotEmpty()) {
            liveOxiMachines.value?.forEach {
                val bleDevice = listToLiveData.find { liveDevice -> liveDevice.macId == it.macId }
                if (bleDevice == null) {
                    listSizeChange = true
                    listToLiveData.add(it)
                }
            }
        } else {
            listSizeChange = true
        }
        if (listSizeChange) {
            liveOxiMachines.value = listToLiveData
        }
    }
}