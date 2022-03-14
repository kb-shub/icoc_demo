package com.example.icocdemo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.icocdemo.R
import com.example.icocdemo.databinding.ItemBpMachineBinding
import com.example.icocdemo.models.BPMachine

class BPMachineAdapter(private val selectionCallBack: SelectionCallBack) :
    ListAdapter<BPMachine, BPMachineAdapter.ContentHolder>(Callback()) {

    inner class ContentHolder(private val binding: ItemBpMachineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: BPMachine) {
            binding.run {
                tvItemWeighMacName.text = model.deviceName
                tvItemWeighMacSysValue.text =
                    (model.systolic ?: "N.A").toString()
                tvItemWeighMacDiaValue.text =
                    (model.diastolic ?: "N.A").toString()
                tvItemWeighMacPulseValue.text =
                    (model.pulse ?: "N.A").toString()
                binding.mbItemWeighMacReadValue.setOnClickListener {
                    selectionCallBack.onItemClick(model)
                }
            }
        }
    }

    interface SelectionCallBack {
        fun onItemClick(item: BPMachine)
    }

    internal class Callback : DiffUtil.ItemCallback<BPMachine>() {
        override fun areItemsTheSame(
            oldItem: BPMachine,
            newItem: BPMachine
        ): Boolean {
            return oldItem.macId == newItem.macId
        }

        override fun areContentsTheSame(
            oldItem: BPMachine,
            newItem: BPMachine
        ): Boolean {
            return oldItem.macId == newItem.macId && oldItem.deviceName == newItem.deviceName && oldItem.systolic == newItem.systolic && oldItem.diastolic == newItem.diastolic && oldItem.pulse == newItem.pulse
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentHolder {

        return ContentHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_bp_machine,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContentHolder, position: Int) {
        holder.bind(getItem(position))
    }

}