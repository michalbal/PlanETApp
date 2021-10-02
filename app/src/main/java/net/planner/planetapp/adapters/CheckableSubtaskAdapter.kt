package net.planner.planetapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import net.planner.planetapp.App
import net.planner.planetapp.R
import net.planner.planetapp.databinding.SubtaskEventListItemBinding
import net.planner.planetapp.getHour
import net.planner.planetapp.planner.PlannerEvent

class CheckableSubtaskAdapter (
    private var values: List<PlannerEvent>
) : RecyclerView.Adapter<CheckableSubtaskAdapter.ViewHolder>() {

    var dayEventsWanted = mutableSetOf<PlannerEvent>()

    val dayEventsData = MutableLiveData<List<PlannerEvent>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            SubtaskEventListItemBinding.inflate(
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

        val cardItem = holder.itemView as MaterialCardView
        cardItem.isChecked = true
        dayEventsWanted.add(item)
        dayEventsData.postValue(dayEventsWanted.toList())

        cardItem.setOnClickListener { view ->
            if (cardItem.isChecked) {
                // Card is checked, needs to become unchecked
                dayEventsWanted.remove(item)
                cardItem.isChecked = false
                dayEventsData.postValue(dayEventsWanted.toList())
            } else {
                dayEventsWanted.add(item)
                cardItem.isChecked = true
                dayEventsData.postValue(dayEventsWanted.toList())
            }
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: SubtaskEventListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val eventName: TextView = binding.eventName
        val eventStart: TextView = binding.eventStart
        val eventEnd: TextView = binding.eventEnd

    }


}