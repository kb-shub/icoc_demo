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
import com.example.icocdemo.adapters.WeighMachineAdapter
import com.example.icocdemo.databinding.ActivityDashboardBinding
import com.example.icocdemo.models.BPMachine
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

    override fun onStop() {
        super.onStop()
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
            rvActDashBoardWeighMacs.adapter = weighMachineAdapter
            rvActDashBoardBpMacs.adapter = bpMachineAdapter
        }
    }

    /**
     * Check if the necessary permissions are given
     */
    @SuppressLint("MissingPermission")
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
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            ) {
                showReasonDialog()
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
                    val weighMachineListToLiveData = mutableListOf<WeighMachine>()
                    val bpMachineListToLiveData = mutableListOf<BPMachine>()
                    setupDeviceDiscoveryMap?.forEach {
                        val device = it.value
                        if (device.dataType == "Adult Weight") {
                            weighMachineListToLiveData.add(
                                WeighMachine(
                                    device.macId,
                                    device.deviceName,
                                    null
                                )
                            )
                        } else if (device.dataType == "Blood Pressure") {
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
                    }
                    viewModel.setNewWeighDevices(
                        weighMachineListToLiveData
                    )
                    viewModel.setNewBPDevices(
                        bpMachineListToLiveData
                    )
                }

                override fun onSetupDeviceNotDiscovered(setupDeviceNonDiscoveryMap: HashMap<String, Device>?) {
                    Log.e(TAG, "onSetupDeviceNotDiscovered")
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
}