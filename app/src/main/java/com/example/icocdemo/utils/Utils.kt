package com.example.icocdemo.utils

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.example.icocdemo.models.BPMachineValues

object Utils {
    private const val TYPE_WEIGHING_MACHINE = 0
    private const val TYPE_BLOOD_PRESSURE_MACHINE = 2
    fun checkPermissions(activityContext: Context, vararg permissionStrings: String): Boolean {
        for (permission in permissionStrings) {
            if (ActivityCompat.checkSelfPermission(
                    activityContext,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun getFloat(value: String?): Float {
        return if (value != null && value != "" && value != ".") value.toFloat() else "0".toFloat()
    }

    fun getInt(value: String?): Int {
        return if (value != null && value != "" && value != ".") value.toInt() else "0".toInt()
    }

    /*fun getDeviceType(type: Int): String {
        return when(type){
            TYPE_WEIGHING_MACHINE ->{
                "Weighing Machine"
            }
            TYPE_BLOOD_PRESSURE_MACHINE ->{
                "Blood Pressure Machine"
            }
            else ->{
                "Invalid Machine Type"
            }
        }
    }*/

    fun checkIfGPSIsOn(activityContext: Context): Boolean {
        val manager = activityContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Gives BPMachineValues() from a string
     */
    fun getBpValuesFromDataString(dataString: String): BPMachineValues {
        val systolic = dataString.substringAfter("Systolic: ").substringBefore("\n")
        val diastolic = dataString.substringAfter("Diastolic: ").substringBeforeLast("\n")
        val pulse = dataString.substringAfter("Pulse: ")
        return BPMachineValues(getInt(systolic), getInt(diastolic), getInt(pulse))
    }
}