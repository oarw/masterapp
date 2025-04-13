package com.masterapp.ui.task

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.masterapp.R
import com.masterapp.data.local.entity.Task
import com.masterapp.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onItemClick: (Task) -> Unit,
    private val onCheckboxClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.checkboxCompleted.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCheckboxClick(getItem(position))
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                textViewTitle.text = task.title
                checkboxCompleted.isChecked = task.isCompleted

                // 如果任务已完成，为标题添加删除线
                if (task.isCompleted) {
                    textViewTitle.paintFlags = textViewTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    textViewTitle.paintFlags = textViewTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                // 显示截止日期
                if (task.dueDate != null) {
                    textViewDueDate.text = formatDate(task.dueDate)
                    textViewDueDate.visibility = View.VISIBLE
                } else {
                    textViewDueDate.visibility = View.GONE
                }

                // 设置优先级图标
                when (task.priority) {
                    3 -> {
                        imageViewPriority.setImageResource(R.drawable.ic_priority_high)
                        imageViewPriority.visibility = View.VISIBLE
                    }
                    2 -> {
                        imageViewPriority.setImageResource(R.drawable.ic_priority_medium)
                        imageViewPriority.visibility = View.VISIBLE
                    }
                    1 -> {
                        imageViewPriority.setImageResource(R.drawable.ic_priority_low)
                        imageViewPriority.visibility = View.VISIBLE
                    }
                    else -> {
                        imageViewPriority.visibility = View.GONE
                    }
                }

                // 显示剩余子任务数量
                if (task.subTaskCount > 0) {
                    textViewSubtaskCount.text = "${task.completedSubTaskCount}/${task.subTaskCount}"
                    textViewSubtaskCount.visibility = View.VISIBLE
                } else {
                    textViewSubtaskCount.visibility = View.GONE
                }

                // 如果任务已逾期且未完成，将日期文本设置为红色
                if (task.dueDate != null && task.dueDate.before(Date()) && !task.isCompleted) {
                    textViewDueDate.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.color_overdue)
                    )
                } else {
                    textViewDueDate.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.color_text_secondary)
                    )
                }
            }
        }

        private fun formatDate(date: Date): String {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return format.format(date)
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}