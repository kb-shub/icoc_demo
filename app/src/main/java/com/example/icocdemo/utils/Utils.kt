package com.example.icocdemo.utils

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.example.icocdemo.models.BPMachineValues
import com.example.icocdemo.models.OxiMachineValues

object Utils {
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

    private fun getInt(value: String?): Int {
        return if (value != null && value != "" && value != ".") value.toInt() else "0".toInt()
    }


    fun checkIfGPSIsOn(activityContext: Context): Boolean {
        val manager = activityContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    /**
     * Gives BPMachineValues() from a data string
     */
    fun getBpValuesFromDataString(dataString: String): BPMachineValues {
        val systolic = dataString.substringAfter("Systolic: ").substringBefore("\n")
        val diastolic = dataString.substringAfter("Diastolic: ").substringBeforeLast("\n")
        val pulse = dataString.substringAfter("Pulse: ")
        return BPMachineValues(getInt(systolic), getInt(diastolic), getInt(pulse))
    }

    /**
     * Gives OxiMachineValues from a data String
     */
    fun getOxiValuesFromDataString(dataString: String): OxiMachineValues {
        val spO2 = dataString.substringAfter("SpO2: ").substringBefore("\n")
        val pulse = dataString.substringAfter("Pulse: ")
        return OxiMachineValues(getInt(spO2), getInt(pulse))
    }
}