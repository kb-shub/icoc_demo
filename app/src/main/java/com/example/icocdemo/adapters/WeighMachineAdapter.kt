package com.example.icocdemo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.icocdemo.R
import com.example.icocdemo.databinding.ItemWeighingMachineBinding
import com.example.icocdemo.models.WeighMachine

class WeighMachineAdapter(private val selectionCallBack: SelectionCallBack) :
    ListAdapter<WeighMachine, WeighMachineAdapter.ContentHolder>(Callback()) {

    inner class ContentHolder(private val binding: ItemWeighingMachineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: WeighMachine) {
            binding.run {
                tvItemWeighMacName.text = model.deviceName
                tvItemWeighMacValue.text =
                    if (model.readingValue != null) binding.root.context.getString(
                        R.string.kg_value,
                        String.format("%.2f", model.readingValue)
                    ) else "N.A"
                binding.mbItemWeighMacReadValue.setOnClickListener {
                    selectionCallBack.onItemClick(model)
                }
            }
        }
    }

    interface SelectionCallBack {
        fun onItemClick(item: WeighMachine)
    }

    internal class Callback : DiffUtil.ItemCallback<WeighMachine>() {
        override fun areItemsTheSame(
            oldItem: WeighMachine,
            newItem: WeighMachine
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: WeighMachine,
            newItem: WeighMachine
        ): Boolean {
            return oldItem.macId == newItem.macId && oldItem.deviceName == newItem.deviceName && oldItem.readingValue == newItem.readingValue
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentHolder {

        return ContentHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_weighing_machine,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContentHolder, position: Int) {
        holder.bind(getItem(position))
    }

}