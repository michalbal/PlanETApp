package net.planner.planetapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import net.planner.planetapp.*
import net.planner.planetapp.database.local_database.TaskLocalDB
import net.planner.planetapp.databinding.TaskListItemBinding
import net.planner.planetapp.fragments.HomeFragmentDirections
import net.planner.planetapp.fragments.PreferancesFragmentDirections
import net.planner.planetapp.planner.PlannerTask

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
        val todayMillis = System.currentTimeMillis()
        var subTasksNotPassedNum = 0
        for(event in item.subtaskDates) {
            getMillisFromDate(event)?.let {
                if (it > todayMillis) {
                    subTasksNotPassedNum += 1
                }
            }
        }

        val progress: Double = item.subtaskDates.size / subTasksNotPassedNum.toDouble() * 100

        holder.progressPercent.text = progress.toString()
        holder.progressBar.progress = progress.toInt()

        holder.preferenceName.setOnClickListener { view->
            activity?.runOnUiThread {
                val navController = activity.findNavController(R.id.nav_host_fragment)
                navController.navigate(HomeFragmentDirections.actionHomeFragmentToViewAndEditPreferenceFragment())
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