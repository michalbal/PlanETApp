package net.planner.planetapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.planner.planetapp.App
import net.planner.planetapp.R
import net.planner.planetapp.planner.PlannerEvent
import net.planner.planetapp.databinding.NextEventListItemBinding
import net.planner.planetapp.getHour

class NextEventViewAdapter(
    private var values: List<PlannerEvent>
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
        // TODO add task button visible based on if there is a related task
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: NextEventListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val eventName: TextView = binding.eventName
        val eventStart: TextView = binding.eventStart
        val eventEnd: TextView = binding.eventEnd
        // TODO: Add connection to related task and a way to click on the task and be routed to edit task/view task details page - If task exists

    }

    fun updateEvents(events: List<PlannerEvent>) {
        values = events
        notifyDataSetChanged()
    }
}