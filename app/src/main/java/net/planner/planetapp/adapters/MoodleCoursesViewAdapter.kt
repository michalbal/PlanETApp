package net.planner.planetapp.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import net.planner.planetapp.databinding.FragmentMoodleCoursesItemBinding

class MoodleCoursesViewAdapter(
    private var values: List<String>,
    private val isClickable:  Boolean,
    var courseIds: MutableSet<String> = mutableSetOf()
) : RecyclerView.Adapter<MoodleCoursesViewAdapter.ViewHolder>()  {

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

        holder.courseIdCheckbox.isClickable = isClickable

        holder.courseIdCheckbox.isChecked = courseIds.contains(item)

        holder.courseIdCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                courseIds.add(item)
                if (unwantedCourseIds.contains(item)) {
                    unwantedCourseIds.remove(item)
                }
            } else {
                courseIds.remove(item)
                unwantedCourseIds.add(item)
            }
        }
    }


    fun updateCourses(courses: List<String>, areChecked: Boolean) {
        values = courses
        if(areChecked) {
            courseIds.addAll(courses)
        }
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentMoodleCoursesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var courseIdCheckbox : CheckBox = binding.courseCheckbox
    }

}