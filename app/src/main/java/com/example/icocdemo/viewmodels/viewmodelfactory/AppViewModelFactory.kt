package com.example.icocdemo.viewmodels.viewmodelfactory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.icocdemo.viewmodels.AddDevicesViewModel
import com.example.icocdemo.viewmodels.DashboardViewModel


class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AddDevicesViewModel::class.java) -> {
                AddDevicesViewModel() as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown class name")
        }
    }
}