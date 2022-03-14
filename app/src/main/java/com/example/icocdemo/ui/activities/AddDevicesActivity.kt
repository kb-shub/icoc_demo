package com.example.icocdemo.ui.activities

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.SimpleItemAnimator
import com.atollsolutions.icoc_library.CallbackInterfaces.scanCallback
import com.atollsolutions.icoc_library.Device
import com.atollsolutions.icoc_library.SDK
import com.cazaea.sweetalert.SweetAlertDialog
import com.example.icocdemo.R
import com.example.icocdemo.adapters.BleDeviceAdapter
import com.example.icocdemo.databinding.ActivityAddDevicesBinding
import com.example.icocdemo.models.BleDevice
import com.example.icocdemo.ui.dialogs.SetUpDeviceDialog
import com.example.icocdemo.utils.Utils
import com.example.icocdemo.viewmodels.AddDevicesViewModel
import com.example.icocdemo.viewmodels.viewmodelfactory.AppViewModelFactory

class AddDevicesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDevicesBinding
    private lateinit var bluetoothManager: BluetoothManager
    private var sdk: SDK? = null
    private lateinit var viewModel: AddDevicesViewModel
    private lateinit var adapter: BleDeviceAdapter

    companion object {
        private const val TAG = "AddDevicesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel()
        sdk = SDK(this)
        binding = ActivityAddDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initUi()
        initSDK()
        checkPermissionAndStartScan()
        observeViewModel()
        onClickListener()
    }

    /**
     * Setup recyclerview and its components
     */
    private fun initUi() {
        setSupportActionBar(binding.tbActAddDevicesToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.run {
            adapter = BleDeviceAdapter(object : BleDeviceAdapter.SelectionCallBack {
                override fun onItemClick(item: BleDevice) {
                    if (sdk != null) {
                        if (sdk!!.isScanning) {
                            Toast.makeText(
                                this@AddDevicesActivity,
                                "Please stop scan first to Add Device",
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }
                        val setUpDeviceDialog = SetUpDeviceDialog(item)
                        setUpDeviceDialog.show(supportFragmentManager, "")
                    }
                }
            })
            rvAddDevicesDevices.adapter = adapter
            (binding.rvAddDevicesDevices.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
                false
        }
    }

    /**
     * Destroy sdk object
     */


    /**
     * Live data observer to feed data to recyclerview
     */
    private fun observeViewModel() {
        viewModel.bleDevices.observe(this) {
            adapter.submitList(it)
        }
    }

    private fun initViewModel() {
        viewModel =
            ViewModelProvider(this, AppViewModelFactory(application)).get(
                AddDevicesViewModel::class.java
            )
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
            if (!bluetoothManager.adapter.isEnabled) {
                bluetoothManager.adapter.enable()
            }
            if (sdk != null) {
                if (sdk!!.isScanning) {
                    sdk!!.stopScan()
                }
            }
            startScan()
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
        StartActivityForResult()
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

    /**
     * Initialize BLE SDK
     */
    private fun initSDK() {
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        sdk?.initSdk(bluetoothManager.adapter, this)
    }

    /**
     * Click listeners
     */
    private fun onClickListener() {
        binding.mbActMainScan.setOnClickListener {
            if (sdk != null) {
                if (sdk!!.isScanning) {
                    sdk!!.stopScan()
                    binding.mbActMainScan.setText(R.string.start_scan)
                } else {
                    checkPermissionAndStartScan()
                    binding.mbActMainScan.setText(R.string.stop_scan)
                }
            }
        }
        binding.tbActAddDevicesToolbar.setNavigationOnClickListener {
            finish()
        }
    }


    override fun onDestroy() {
        if (sdk != null) {
            sdk!!.stopScan()
            sdk!!.deInit(this)
        }
        super.onDestroy()
    }

    /**
     * Starts looking for the available bluetooth Devices
     * This method is suppose to give all
     */
    private fun startScan() {
        if (Utils.checkIfGPSIsOn(this)) {
            sdk?.startScan(object : scanCallback {
                @SuppressLint("MissingPermission")
                override fun onDeviceDiscovered(scanResultMap: HashMap<String, ScanResult>?) {
                    if (scanResultMap != null) {
                        val listToLiveData = mutableListOf<BleDevice>()
                        scanResultMap.forEach {
                            val scanResult = it.value
                            val device = scanResult.device
                            listToLiveData.add(
                                BleDevice(
                                    device.address.toString(),
                                    if (device.name != null) device.name else "Not found!",
                                    scanResult.rssi.toString(),
                                    com.atollsolutions.icoc_library.Utils.getDataType(device)
                                )
                            )
                        }
                        viewModel.bleDevices.value =
                            setNewDevices(listToLiveData, viewModel.bleDevices.value!!)
                    }
                }

                override fun onSetupDeviceDiscovered(
                    setupDeviceDiscoveryMap: HashMap<String, Device>?,
                    scanResultMap: HashMap<String, ScanResult>?
                ) {
                    Log.e(TAG, "onSetupDeviceDiscovered")
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

    private var checkSettingsLauncher = registerForActivityResult(
        StartActivityForResult()
    ) {
        startScan()
    }

    /**
     * Live data list filterer
     */
    private fun setNewDevices(
        listToLiveData: MutableList<BleDevice>,
        value: MutableList<BleDevice>
    ): MutableList<BleDevice> {
        value.forEach {
            val bleDevice = listToLiveData.find { liveDevice -> liveDevice.macId == it.macId }
            if (bleDevice == null) {
                listToLiveData.add(it)
            }
        }
        return listToLiveData
    }
}