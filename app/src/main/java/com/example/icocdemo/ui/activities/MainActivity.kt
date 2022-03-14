package com.example.icocdemo.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.icocdemo.databinding.ActivityMainBinding

/**
 * Start Activity with buttons to navigate to pair and check the values given by devices
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onClickListeners()
    }

    private fun onClickListeners() {
        binding.mbActMainAddDevices.setOnClickListener {
            startActivity(Intent(this, AddDevicesActivity::class.java))
        }
        binding.mbActMainDashboard.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
    }
}