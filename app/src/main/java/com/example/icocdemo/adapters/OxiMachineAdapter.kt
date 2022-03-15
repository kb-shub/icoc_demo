package com.example.icocdemo.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.icocdemo.R
import com.example.icocdemo.databinding.ItemOxiMachineBinding
import com.example.icocdemo.models.OxiMachine

class OxiMachineAdapter(private val selectionCallBack: SelectionCallBack) :
    ListAdapter<OxiMachine, OxiMachineAdapter.ContentHolder>(Callback()) {
    companion object {
        private const val TAG = "OxiMachineAdapter"
    }

    inner class ContentHolder(private val binding: ItemOxiMachineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: OxiMachine) {
            binding.run {
                if (model.isConnected) binding.tvItemOxiMacSpO2Holder.alpha =
                    1.0f else binding.tvItemOxiMacSpO2Holder.alpha = .3f
                if (model.isConnected) binding.tvItemOxiMacSpO2Value.alpha =
                    1.0f else binding.tvItemOxiMacSpO2Value.alpha = .3f
                if (model.isConnected) binding.tvItemOxiMacPulseHolder.alpha =
                    1.0f else binding.tvItemOxiMacPulseHolder.alpha = .3f
                if (model.isConnected) binding.tvItemOxiMacPulseValue.alpha =
                    1.0f else binding.tvItemOxiMacPulseValue.alpha = .3f
                if (model.isConnected) binding.mbItemOxiMacReadValue.alpha =
                    1.0f else binding.mbItemOxiMacReadValue.alpha = .3f
                mbItemOxiMacReadValue.isEnabled = model.isConnected
                tvItemOxiMacName.text = model.deviceName
                tvItemOxiMacSpO2Value.text =
                    (model.spO2 ?: "N.A").toString()
                tvItemOxiMacPulseValue.text =
                    (model.pulse ?: "N.A").toString()
                binding.mbItemOxiMacReadValue.setOnClickListener {
                    selectionCallBack.onItemClick(model)
                }
            }
        }
    }

    interface SelectionCallBack {
        fun onItemClick(item: OxiMachine)
    }

    internal class Callback : DiffUtil.ItemCallback<OxiMachine>() {
        override fun areItemsTheSame(
            oldItem: OxiMachine,
            newItem: OxiMachine
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: OxiMachine,
            newItem: OxiMachine
        ): Boolean {
            return oldItem.macId == newItem.macId && oldItem.deviceName == newItem.deviceName && oldItem.spO2 == newItem.spO2 && oldItem.pulse == newItem.pulse && oldItem.isConnected == newItem.isConnected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentHolder {

        return ContentHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_oxi_machine,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: ContentHolder, position: Int) {
        holder.bind(getItem(position))
    }

}