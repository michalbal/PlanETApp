package net.planner.planetapp.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MotionEventCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.planner.planetapp.MainActivity
import net.planner.planetapp.R
import net.planner.planetapp.database.local_database.PreferencesLocalDB
import net.planner.planetapp.databinding.PreferancesListItemBinding


/**
 * [RecyclerView.Adapter] that can display Preferances.
 */
class PreferancesViewAdapter(
    private var values: List<PreferencesLocalDB>,
    private val activity: MainActivity?
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
        coursesRecycler.adapter = MoodleCoursesViewAdapter(item.courses.toList(), false, item.courses.toMutableSet())
        coursesRecycler.isNestedScrollingEnabled = true
        holder.name.text = item.tagName
        holder.itemView.setOnClickListener { view ->
            activity?.runOnUiThread {
                val navController = activity.findNavController(R.id.nav_host_fragment)
                val bundle = bundleOf("preferenceName" to item.tagName)
                navController.navigate(R.id.createPreferenceFragment, bundle)
            }
        }
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