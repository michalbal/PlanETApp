package net.planner.planetapp.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import net.planner.planetapp.*
import net.planner.planetapp.database.local_database.TaskLocalDB
import net.planner.planetapp.databinding.TaskListItemBinding
class TasksViewAdapter(
    private var values: List<TaskLocalDB>,
    private val activity: MainActivity?
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
        holder.taskTitle.text = item.name
        holder.dueDate.text = String.format(App.context.getString(R.string.due_date), getDate(item.deadline))
        holder.preferenceName.text = item.tag

        // Calculate progress
        val todayMillis = getTodayTimeMillis()
        var subTasksPassedNum = 0
        for(event in item.subtaskDates) {
            getMillisFromDate(event)?.let {
                if (it >= todayMillis) {
                    subTasksPassedNum += 1
                }
            }
        }

        var progress = 0
        if (subTasksPassedNum != 0) {
            progress = (item.subtaskDates.size / subTasksPassedNum.toDouble() * 100).toInt()
        }

        holder.progressPercent.text = "$progress%"
        holder.progressBar.progress = progress

        holder.preferenceName.setOnClickListener { view->
            activity?.runOnUiThread {
                val navController = activity.findNavController(R.id.nav_host_fragment)
                val bundle = bundleOf("preferenceName" to item.tag)
                navController.navigate(R.id.createPreferenceFragment, bundle)
            }
        }

        holder.itemView.setOnClickListener { view ->
            activity?.runOnUiThread {
                val navController = activity.findNavController(R.id.nav_host_fragment)
                val bundle = bundleOf("deadlineDateStart" to item.deadline, "taskId" to item.taskId)
                navController.navigate(R.id.createTaskFragment, bundle)
            }
        }

    }

    override fun getItemCount(): Int = values.size

    fun updateTasks(tasksSent: List<TaskLocalDB>) {
        values = tasksSent
        notifyDataSetChanged()
    }

    inner class ViewHolder(binding: TaskListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val taskTitle: TextView = binding.taskTitle
        val dueDate: TextView = binding.taskDueDate
        val preferenceName: Button = binding.preferenceButton
        val progressPercent: TextView = binding.progressPercent
        val progressBar: LinearProgressIndicator = binding.taskProgress
    }
}