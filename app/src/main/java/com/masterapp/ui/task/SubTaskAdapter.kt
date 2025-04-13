package com.masterapp.ui.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.masterapp.data.local.entity.SubTask
import com.masterapp.databinding.ItemSubtaskBinding

class SubTaskAdapter(
    private val onCompletedChanged: (SubTask) -> Unit,
    private val onDeleteClick: (SubTask) -> Unit
) : ListAdapter<SubTask, SubTaskAdapter.SubTaskViewHolder>(SubTaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubTaskViewHolder {
        val binding = ItemSubtaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubTaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubTaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubTaskViewHolder(private val binding: ItemSubtaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.checkboxSubtaskCompleted.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCompletedChanged(getItem(position))
                }
            }

            binding.buttonDeleteSubtask.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(subtask: SubTask) {
            binding.apply {
                textViewSubtaskTitle.text = subtask.title
                checkboxSubtaskCompleted.isChecked = subtask.isCompleted

                // 如果子任务已完成，添加删除线
                if (subtask.isCompleted) {
                    textViewSubtaskTitle.paintFlags = textViewSubtaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    textViewSubtaskTitle.paintFlags = textViewSubtaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }
            }
        }
    }

    class SubTaskDiffCallback : DiffUtil.ItemCallback<SubTask>() {
        override fun areItemsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
            return oldItem == newItem
        }
    }
}