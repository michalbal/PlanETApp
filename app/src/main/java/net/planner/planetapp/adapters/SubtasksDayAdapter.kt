package net.planner.planetapp.adapters

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.planner.planetapp.R
import net.planner.planetapp.databinding.DayPlanItemBinding
import net.planner.planetapp.planner.PlannerEvent

class SubtasksDayAdapter(
    private var values: List<SubtaskPlanDayRep>,
    private val owner: LifecycleOwner
) : RecyclerView.Adapter<SubtasksDayAdapter.ViewHolder>() {

    private var subtasksDayList: List<SubtaskPlanDayRep> = values

    private val scrollStates: MutableMap<String, Parcelable?> = mutableMapOf()

    private fun getSectionID(position: Int): String {
        return values[position].dayDate
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            DayPlanItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onViewRecycled(holder: SubtasksDayAdapter.ViewHolder) {
        super.onViewRecycled(holder)
        // Save scroll state
        val key = getSectionID(holder.layoutPosition)
        scrollStates[key] = holder.itemView.findViewById<RecyclerView>(R.id.sub_tasks_day_list).layoutManager?.onSaveInstanceState()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        // Restore scroll state
        val key = getSectionID(holder.layoutPosition)
        val state = scrollStates[key]
        if (state != null) {
            holder.subtasksRecycler.layoutManager?.onRestoreInstanceState(state)
        } else {
            holder.subtasksRecycler.layoutManager?.scrollToPosition(0)
        }

        // Init Subtasks Recycler View
        holder.subtasksRecycler = holder.subtasksRecycler
        holder.subtasksRecycler.layoutManager = LinearLayoutManager(holder.subtasksRecycler.context)
        val subtasksDayAdapter = CheckableSubtaskAdapter(item.subtasks)
        holder.subtasksRecycler.adapter = subtasksDayAdapter

        holder.dayDateTitle.text = item.dayDate

        subtasksDayAdapter.dayEventsData.observe(owner, Observer { it?.let {
            subtasksDayList[position].subtasks = it
            }
        })

    }

    fun getEventsApproved(): List<PlannerEvent> {
        val eventList = mutableListOf<PlannerEvent>()
        for(day in subtasksDayList) {
            eventList.addAll(day.subtasks)
        }
        return eventList
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: DayPlanItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var dayDateTitle: TextView = binding.dayDate
        var subtasksRecycler: RecyclerView = binding.subTasksDayList
    }

}

data class SubtaskPlanDayRep(
    val dayDate: String,
    var subtasks: List<PlannerEvent>

)