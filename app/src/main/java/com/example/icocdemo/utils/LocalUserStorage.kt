package com.example.icocdemo.utils

import android.content.Context

object LocalUserStorage {

    @JvmStatic
    fun loadUserData(context: Context): PreferenceUtil {
        val sharedPreferences = context.getSharedPreferences("local_user", Context.MODE_PRIVATE)
        return PreferenceUtil(
            sharedPreferences.getBoolean("permission_rationale", false)
        )
    }

    @JvmStatic
    fun saveUserData(context: Context, preferenceModel: PreferenceUtil) {
        val sharedPreferences = context.getSharedPreferences("local_user", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("permission_rationale", preferenceModel.permissionRationale)
        editor.apply()
    }

    @JvmStatic
    fun savePermissionRationale(context: Context, permissionRationale: Boolean) {
        val sharedPreferences = context.getSharedPreferences("local_user", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("permission_rationale", permissionRationale)
        editor.apply()
    }


}