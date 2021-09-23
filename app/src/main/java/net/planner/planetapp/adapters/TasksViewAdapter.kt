package net.planner.planetapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import net.planner.planetapp.App
import net.planner.planetapp.R
import net.planner.planetapp.databinding.TaskListItemBinding
import net.planner.planetapp.getDate
import net.planner.planetapp.planner.PlannerTask

class TasksViewAdapter(
    private val values: List<PlannerTask>
) : RecyclerView.Adapter<TasksViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            TaskListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.taskTitle.text = item.title
        holder.dueDate.text = String.format(App.context.getString(R.string.due_date), getDate(item.deadline))

        // TODO add progress to Task either by adding it directly or by adding events to it's
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: TaskListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val taskTitle: TextView = binding.taskTitle
        val dueDate: TextView = binding.taskDueDate
        val progressPercent: TextView = binding.progressPercent
        val progressBar: LinearProgressIndicator = binding.taskProgress
    }
}