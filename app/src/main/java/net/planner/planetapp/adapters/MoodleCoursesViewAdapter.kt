package net.planner.planetapp.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import net.planner.planetapp.databinding.FragmentMoodleCoursesItemBinding

class MoodleCoursesViewAdapter(
    private var values: List<String>
) : RecyclerView.Adapter<MoodleCoursesViewAdapter.ViewHolder>()  {

    var courseIds: MutableSet<String> = mutableSetOf()
    var unwantedCourseIds: MutableSet<String> = mutableSetOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodleCoursesViewAdapter.ViewHolder {

        return ViewHolder(
            FragmentMoodleCoursesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.courseIdCheckbox.text = item

        holder.courseIdCheckbox.setOnClickListener { view ->
            if (holder.courseIdCheckbox.isChecked) {
                courseIds.remove(item)
                unwantedCourseIds.add(item)
            } else {
                courseIds.add(item)
                if (unwantedCourseIds.contains(item)) {
                    unwantedCourseIds.remove(item)
                }
            }
        }
        }


    fun updateCourses(courses: List<String>) {
        values = courses
        courseIds.addAll(courses)
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentMoodleCoursesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var courseIdCheckbox : CheckBox = binding.courseCheckbox
    }

}