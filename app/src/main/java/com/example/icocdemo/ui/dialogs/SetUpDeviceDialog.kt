package com.example.icocdemo.ui.dialogs

import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.atollsolutions.icoc_library.Device
import com.atollsolutions.icoc_library.SDK
import com.example.icocdemo.databinding.DialogSetUpDeviceBinding
import com.example.icocdemo.models.BleDevice

class SetUpDeviceDialog(private val device: BleDevice) : DialogFragment() {

    private lateinit var sdk: SDK
    private lateinit var binding: DialogSetUpDeviceBinding
    private lateinit var bluetoothManager: BluetoothManager

    private fun initSDK() {
        bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        sdk.initSdk(bluetoothManager.adapter, requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogSetUpDeviceBinding.inflate(inflater)
        binding.run {
            tvDiaSetUpDeviceSetDeviceMacId.text = device.macId
            tvDiaSetUpDeviceSetDeviceMachineType.text = device.bleMachineName
        }
        sdk = SDK(requireContext())
        initSDK()
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        sdk.deInit(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setCancelable(false)
        onCLickListeners()
    }

    private fun onCLickListeners() {
        binding.mbDiaSetUpDeviceOk.setOnClickListener {
            if (!binding.pgDiaSetUpDeviceProgress.isVisible) {
                binding.pgDiaSetUpDeviceProgress.isVisible = true
                sdk.setupDevice(
                    Device(
                        device.macId,
                        binding.etDiaSetUpDeviceDeviceName.text.toString(),
                        device.bleMachineName
                    ),
                    requireContext(),
                    object : com.atollsolutions.icoc_library.CallbackInterfaces.setupCallback {
                        override fun onDeviceSetupCompleted(device: Device?) {
                            Toast.makeText(
                                requireContext(),
                                "Device Setup Completed",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.pgDiaSetUpDeviceProgress.isVisible = false
                            dismiss()
                        }

                        override fun onExistingNameError(device: Device?) {
                            Toast.makeText(
                                requireContext(),
                                "Please use other name. Device with this name already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.pgDiaSetUpDeviceProgress.isVisible = false
                            dismiss()
                        }

                        override fun onExistingDeviceError(device: Device?) {
                            Toast.makeText(
                                requireContext(),
                                "Device with this id already exists",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.pgDiaSetUpDeviceProgress.isVisible = false
                            dismiss()
                        }

                        override fun onDeviceSetupError(device: Device?, status: Int) {
                            Toast.makeText(
                                requireContext(),
                                "INVALID ERROR!!",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.pgDiaSetUpDeviceProgress.isVisible = false
                            dismiss()
                        }

                    });

            } else {
                Toast.makeText(
                    requireContext(),
                    "Please wait. Data saving in progress",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.mbDiaSetUpDeviceCancel.setOnClickListener {
            if (!binding.pgDiaSetUpDeviceProgress.isVisible) {
                dialog?.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please wait. Data saving in progress",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}