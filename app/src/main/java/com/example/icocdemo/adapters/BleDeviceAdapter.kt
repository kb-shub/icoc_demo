package com.example.icocdemo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.icocdemo.R
import com.example.icocdemo.databinding.ItemBluetoothDeviceBinding
import com.example.icocdemo.models.BleDevice

class BleDeviceAdapter(private val selectionCallBack: SelectionCallBack) :
    ListAdapter<BleDevice, BleDeviceAdapter.ContentHolder>(Callback()) {

    inner class ContentHolder(private val binding: ItemBluetoothDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: BleDevice) {
            binding.run {
                tvItemBluetoothDeviceMacId.text = model.macId
                tvItemBluetoothDeviceMachineTypeCode.text = model.bleMachineType
                tvItemBluetoothDeviceRSSI.text =
                    binding.root.context.getString(R.string.rssi_s, model.bleRSSI)
                tvItemBluetoothDeviceMachineTypeName.text = model.bleMachineName
                binding.clAddDevicesRoot.setOnClickListener {
                    selectionCallBack.onItemClick(model)
                }
            }
        }
    }

    interface SelectionCallBack {
        fun onItemClick(item: BleDevice)
    }

    internal class Callback : DiffUtil.ItemCallback<BleDevice>() {
        override fun areItemsTheSame(
            oldItem: BleDevice,
            newItem: BleDevice
        ): Boolean {
            return oldItem.macId == newItem.macId
        }

        override fun areContentsTheSame(
            oldItem: BleDevice,
            newItem: BleDevice
        ): Boolean {
            return oldItem.macId == newItem.macId && oldItem.bleMachineType == newItem.bleMachineType && oldItem.bleMachineName == newItem.bleMachineName && oldItem.bleRSSI == newItem.bleRSSI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentHolder {

        return ContentHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_bluetooth_device,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContentHolder, position: Int) {
        holder.bind(getItem(position))
    }

}