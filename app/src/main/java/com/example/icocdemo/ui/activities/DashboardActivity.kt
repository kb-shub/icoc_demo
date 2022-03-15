package com.example.icocdemo.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.atollsolutions.icoc_library.CallbackInterfaces.DataCallback
import com.atollsolutions.icoc_library.CallbackInterfaces.scanCallback
import com.atollsolutions.icoc_library.Device
import com.atollsolutions.icoc_library.SDK
import com.cazaea.sweetalert.SweetAlertDialog
import com.example.icocdemo.R
import com.example.icocdemo.adapters.BPMachineAdapter
import com.example.icocdemo.adapters.OxiMachineAdapter
import com.example.icocdemo.adapters.WeighMachineAdapter
import com.example.icocdemo.databinding.ActivityDashboardBinding
import com.example.icocdemo.models.BPMachine
import com.example.icocdemo.models.OxiMachine
import com.example.icocdemo.models.WeighMachine
import com.example.icocdemo.utils.Utils
import com.example.icocdemo.viewmodels.DashboardViewModel
import com.example.icocdemo.viewmodels.viewmodelfactory.AppViewModelFactory

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private var sdk: SDK? = null
    private var bluetoothManager: BluetoothManager? = null
    private lateinit var weighMachineAdapter: WeighMachineAdapter
    private lateinit var bpMachineAdapter: BPMachineAdapter
    private lateinit var oxiMachineAdapter: OxiMachineAdapter

    companion object {
        private const val TAG = "DashboardActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        sdk = SDK(this)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initSDK()
        initUI()
        checkPermissionAndStartScan()
        observeViewModel()
        clickListeners()
    }

    private fun clickListeners() {
        binding.tbActDashDevicesToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (sdk != null) {
            sdk!!.stopScan()
            sdk!!.deInit(this)
        }
    }

    private fun observeViewModel() {
        viewModel.liveWeighMachines.observe(this) {
            binding.tvActDashBoardWeighMacs.isVisible = it.isEmpty()
            weighMachineAdapter.submitList(it)
            weighMachineAdapter.notifyDataSetChanged()
        }
        viewModel.liveBPMachines.observe(this) {
            binding.tvActDashBoardBPMacs.isVisible = it.isEmpty()
            bpMachineAdapter.submitList(it)
            bpMachineAdapter.notifyDataSetChanged()
        }
        viewModel.liveOxiMachines.observe(this) {
            binding.tvActDashBoardOxiMacs.isVisible = it.isEmpty()
            oxiMachineAdapter.submitList(it)
            oxiMachineAdapter.notifyDataSetChanged()
        }
    }

    private fun initUI() {
        setSupportActionBar(binding.tbActDashDevicesToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.run {
            weighMachineAdapter =
                WeighMachineAdapter(object : WeighMachineAdapter.SelectionCallBack {
                    override fun onItemClick(item: WeighMachine) {
                        readWeighingMachineValue(item)
                    }
                })
            bpMachineAdapter = BPMachineAdapter(object : BPMachineAdapter.SelectionCallBack {
                override fun onItemClick(item: BPMachine) {
                    readBPMachineValue(item)
                }
            })
            oxiMachineAdapter = OxiMachineAdapter(object : OxiMachineAdapter.SelectionCallBack {
                override fun onItemClick(item: OxiMachine) {
                    readOxiMachineValues(item)
                }
            })
            rvActDashBoardWeighMacs.adapter = weighMachineAdapter
            rvActDashBoardBpMacs.adapter = bpMachineAdapter
            rvActDashBoardOxiMacs.adapter = oxiMachineAdapter
        }
    }

    /**
     * Check if the necessary permissions are given
     */
    private fun checkPermissionAndStartScan() {
        if (!Utils.checkPermissions(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        ) {
            requestPermissions()
        } else {
            checkStartScan()
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkStartScan() {
        if (bluetoothManager != null && bluetoothManager?.adapter != null) {
            if (!bluetoothManager?.adapter?.isEnabled!!) {
                bluetoothManager?.adapter?.enable()
            }
            if (sdk != null) {
                if (sdk!!.isScanning) {
                    sdk!!.stopScan()
                }
            }
            startScan()
        }
    }


    private val multiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (!Utils.checkPermissions(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                showReasonDialog()
            } else {
                checkStartScan()
            }
        }

    /**
     * Request necessary permissions
     */
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            multiplePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        } else {
            multiplePermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    /**
     * Starts looking for the available bluetooth Devices
     * This method is suppose to give all
     */
    private fun startScan() {
        if (Utils.checkIfGPSIsOn(this)) {
            sdk?.startScan(object : scanCallback {
                override fun onDeviceDiscovered(scanResultMap: HashMap<String, ScanResult>?) {
                    Log.e(TAG, "onDeviceDiscovered")
                }

                @SuppressLint("MissingPermission")
                override fun onSetupDeviceDiscovered(
                    setupDeviceDiscoveryMap: HashMap<String, Device>?,
                    scanResultMap: HashMap<String, ScanResult>?
                ) {
                    if (setupDeviceDiscoveryMap != null) {
                        val weighMachineListToLiveData = mutableListOf<WeighMachine>()
                        val bpMachineListToLiveData = mutableListOf<BPMachine>()
                        val oxiMachineListToLiveData = mutableListOf<OxiMachine>()
                        setupDeviceDiscoveryMap.forEach {
                            val device = it.value
                            when (device.dataType) {
                                "Adult Weight" -> {
                                    weighMachineListToLiveData.add(
                                        WeighMachine(
                                            device.macId,
                                            device.deviceName,
                                            null
                                        )
                                    )
                                }
                                "Blood Pressure" -> {
                                    bpMachineListToLiveData.add(
                                        BPMachine(
                                            device.macId,
                                            device.deviceName,
                                            null,
                                            null,
                                            null
                                        )
                                    )
                                }
                                "Pulse Oximetry" -> {
                                    oxiMachineListToLiveData.add(
                                        OxiMachine(
                                            device.macId,
                                            device.deviceName,
                                            null,
                                            null
                                        )
                                    )
                                }
                            }
                        }
                        viewModel.setNewWeighDevices(
                            weighMachineListToLiveData
                        )
                        viewModel.setNewBPDevices(
                            bpMachineListToLiveData
                        )
                        viewModel.setNewOxiDevices(oxiMachineListToLiveData)

                        setupDeviceDiscoveryMap.forEach {
                            val device = it.value
                            val oxiMachines = viewModel.liveOxiMachines.value
                            var listChange = false
                            oxiMachines?.forEach { oxiMac ->
                                if (oxiMac.macId == device.macId) {
                                    if (!oxiMac.isConnected) {
                                        listChange = true
                                        oxiMac.isConnected = true
                                    }
                                }
                            }
                            if (listChange) {
                                runOnUiThread {
                                    viewModel.liveOxiMachines.value = oxiMachines
                                }
                            }
                        }
                    }
                }

                override fun onSetupDeviceNotDiscovered(setupDeviceNonDiscoveryMap: HashMap<String, Device>?) {
                    setupDeviceNonDiscoveryMap?.forEach {
                        val device = it.value
                        val oxiMachines = viewModel.liveOxiMachines.value
                        var listChange = false
                        oxiMachines?.forEach { oxiMac ->
                            if (oxiMac.macId == device.macId) {
                                if (oxiMac.isConnected) {
                                    listChange = true
                                    oxiMac.isConnected = false
                                }
                            }
                        }
                        if (listChange) {
                            runOnUiThread {
                                viewModel.liveOxiMachines.value = oxiMachines
                            }
                        }
                    }
                }

                override fun onScanError(errorCode: Int) {
                    Log.e(TAG, "onScanError")
                }
            })
        } else {
            val callGPSSettingIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            checkSettingsLauncher.launch(callGPSSettingIntent)
        }
    }

    /**
     * Initialize BLE SDK
     */
    private fun initSDK() {
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager == null || bluetoothManager?.adapter == null) {
            Toast.makeText(this, "Bluetooth not found in device", Toast.LENGTH_SHORT).show()
            finish()
            setResult(Activity.RESULT_CANCELED)
        } else {
            if (sdk == null) {
                sdk = SDK(this)
            }
            sdk?.initSdk(bluetoothManager?.adapter, this)
        }
    }

    private var checkSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        startScan()
    }

    /**
     * Dialog for reasoning of asking for permissions
     */
    private fun showReasonDialog() {
        val sweetAlertDialog = SweetAlertDialog(this, 3)
        sweetAlertDialog.setCancelable(false)
        sweetAlertDialog.titleText = getString(R.string.settings_dialog_title)
        sweetAlertDialog.contentText =
            "This permissions are necessary to perform scanning and adding bluetooth devices."
        sweetAlertDialog.confirmText = "Exit"
        sweetAlertDialog.cancelText = "Settings"
        sweetAlertDialog.setCancelClickListener {
            val settingsIntent = Intent()
            settingsIntent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            settingsIntent.addCategory(Intent.CATEGORY_DEFAULT)
            settingsIntent.data = Uri.parse("package:" + this.packageName)
            sweetAlertDialog.cancel()
            verifyLocationFromSettingsLauncher.launch(settingsIntent)
        }
        sweetAlertDialog.setConfirmClickListener {
            this.finish()
        }
        sweetAlertDialog.show()
    }

    private var verifyLocationFromSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!Utils.checkPermissions(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        ) {
            showReasonDialog()
        } else {
            checkPermissionAndStartScan()
        }
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProvider(this, AppViewModelFactory(application)).get(
                DashboardViewModel::class.java
            )
    }

    private fun readWeighingMachineValue(item: WeighMachine) {
        sdk?.getData(
            Device(item.macId, item.deviceName, "Adult Weight"),
            this,
            object : DataCallback {
                override fun onDeviceConnected(device: Device?) {
                    Log.e(TAG, "onDeviceConnected")
                }

                override fun onDataReceived(device: Device?, data: String?) {
                    val weighMachines = viewModel.liveWeighMachines.value
                    weighMachines?.forEach {
                        if (it.macId == device?.macId) {
                            it.readingValue = Utils.getFloat(data)
                        }
                    }
                    viewModel.liveWeighMachines.value = weighMachines
                }

                override fun onDeviceDisconnected(device: Device?) {
                    Log.e(TAG, "onDeviceDisconnected")
                }

                override fun onError(device: Device?, error: String?) {
                    Log.e(TAG, "onError")
                }
            })
    }

    private fun readBPMachineValue(item: BPMachine) {
        sdk?.getData(
            Device(item.macId, item.deviceName, "Blood Pressure"),
            this,
            object : DataCallback {
                override fun onDeviceConnected(device: Device?) {
                    Log.e(TAG, "onDeviceConnected")
                }

                override fun onDataReceived(device: Device?, data: String?) {
                    val bpMachines = viewModel.liveBPMachines.value
                    bpMachines?.forEach {
                        if (it.macId == device?.macId && data != null) {
                            val bpMachineValues = Utils.getBpValuesFromDataString(data)
                            it.systolic = bpMachineValues.systolic
                            it.diastolic = bpMachineValues.diastolic
                            it.pulse = bpMachineValues.pulse
                        }
                    }
                    viewModel.liveBPMachines.value = bpMachines
                }

                override fun onDeviceDisconnected(device: Device?) {
                    Log.e(TAG, "onDeviceDisconnected")
                }

                override fun onError(device: Device?, error: String?) {
                    Log.e(TAG, "onError")
                }
            })
    }

    private fun readOxiMachineValues(item: OxiMachine) {
        sdk?.getData(
            Device(item.macId, item.deviceName, "Pulse Oximetry"),
            this,
            object : DataCallback {
                override fun onDeviceConnected(device: Device?) {}

                override fun onDataReceived(device: Device?, data: String?) {
                    val oxiMachines = viewModel.liveOxiMachines.value
                    oxiMachines?.forEach {
                        if (it.macId == device?.macId && data != null) {
                            val bpMachineValues = Utils.getOxiValuesFromDataString(data)
                            it.spO2 = bpMachineValues.spO2
                            it.pulse = bpMachineValues.pulse
                        }
                    }
                    runOnUiThread {
                        viewModel.liveOxiMachines.value = oxiMachines
                    }
                }

                override fun onDeviceDisconnected(device: Device?) {}

                override fun onError(device: Device?, error: String?) {
                    Log.e(TAG, "onError")
                }
            })
    }
}