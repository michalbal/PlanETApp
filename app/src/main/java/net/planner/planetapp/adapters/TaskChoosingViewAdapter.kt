package net.planner.planetapp.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import net.planner.planetapp.App
import net.planner.planetapp.R
import net.planner.planetapp.databinding.FragmentMoodleCoursesItemBinding
import net.planner.planetapp.planner.PlannerTask

class TaskChoosingViewAdapter (
    private var values: List<PlannerTask>
) : RecyclerView.Adapter<TaskChoosingViewAdapter.ViewHolder>()  {

    var tasksToPlan: ArrayList<PlannerTask> = ArrayList(values)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskChoosingViewAdapter.ViewHolder {

        return ViewHolder(
            FragmentMoodleCoursesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: TaskChoosingViewAdapter.ViewHolder, position: Int) {
        val item = values[position]
        holder.taskCheckbox.text = item.title
        holder.taskCheckbox.buttonTintList = ColorStateList.valueOf(ContextCompat.getColor(App.context, R.color.colorPrimary))

        holder.taskCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!tasksToPlan.contains(item)) {
                    tasksToPlan.add(item)
                }
            } else {
                tasksToPlan.remove(item)
            }
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentMoodleCoursesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var taskCheckbox : CheckBox = binding.courseCheckbox
    }


}