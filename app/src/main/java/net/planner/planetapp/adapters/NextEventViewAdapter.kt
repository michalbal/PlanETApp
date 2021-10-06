package net.planner.planetapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import net.planner.planetapp.*
import net.planner.planetapp.planner.PlannerEvent
import net.planner.planetapp.databinding.NextEventListItemBinding

class NextEventViewAdapter(
    private var values: List<PlannerEvent>,
    private val activity: MainActivity?
) : RecyclerView.Adapter<NextEventViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            NextEventListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.eventName.text = item.title
        App.context.getString(R.string.starts_at)
        holder.eventStart.text = String.format(App.context.getString(R.string.starts_at), getHour(item.startTime))
        holder.eventEnd.text = String.format(App.context.getString(R.string.ends_at), getHour(item.endTime))

        val taskId = item.parentTaskId
        if (taskId == "") {
            holder.taskButton.visibility = View.INVISIBLE
        } else {
            holder.taskButton.visibility = View.VISIBLE
            holder.taskButton.setOnClickListener { view->
                activity?.runOnUiThread {
                    val navController = activity.findNavController(R.id.nav_host_fragment)
                    val bundle = bundleOf("deadlineDateStart" to getTodayTimeMillis(), "taskId" to item.parentTaskId)
                    navController.navigate(R.id.createTaskFragment, bundle)
                }
            }
        }

    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: NextEventListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val eventName: TextView = binding.eventName
        val eventStart: TextView = binding.eventStart
        val eventEnd: TextView = binding.eventEnd
        val taskButton: Button = binding.taskButton
    }

    fun updateEvents(events: List<PlannerEvent>) {
        values = events
        notifyDataSetChanged()
    }
}