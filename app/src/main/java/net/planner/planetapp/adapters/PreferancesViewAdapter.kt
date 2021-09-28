package net.planner.planetapp.adapters

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.database.local_database.PreferencesLocalDB
import net.planner.planetapp.databinding.PreferancesListItemBinding

/**
 * [RecyclerView.Adapter] that can display Preferances.
 */
class PreferancesViewAdapter(
    private var values: List<PreferencesLocalDB>
) : RecyclerView.Adapter<PreferancesViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            PreferancesListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        // Init Courses Recycler View

        val coursesRecycler = holder.coursesRecycler
        coursesRecycler.layoutManager = LinearLayoutManager(coursesRecycler.context)
        coursesRecycler.adapter = MoodleCoursesViewAdapter(item.courses.toList(), false)
        holder.name.text = item.tagName
    }

    override fun getItemCount(): Int = values.size

    fun updatePreferences(preferences: List<PreferencesLocalDB>) {
        values = preferences
        notifyDataSetChanged()
    }

    inner class ViewHolder(binding: PreferancesListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val coursesRecycler = binding.courseNamesList
        val name = binding.preferenceName

    }

}