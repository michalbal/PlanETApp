package net.planner.planetapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.planner.planetapp.MainActivity
import net.planner.planetapp.R
import net.planner.planetapp.database.local_database.PreferencesLocalDB
import net.planner.planetapp.databinding.PreferancesListItemBinding
import net.planner.planetapp.turnTimesMapIntoListTimeRep

class PreferenceListItemAdapter(
    private var values: List<PreferencesLocalDB>,
    private val fm: FragmentManager,
    private val activity: MainActivity?
) : RecyclerView.Adapter<PreferenceListItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreferenceListItemAdapter.ViewHolder {

        return ViewHolder(
            PreferancesListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: PreferenceListItemAdapter.ViewHolder, position: Int) {
        val item = values[position]

        holder.preferenceName.text = item.tagName
        holder.itemView.setOnClickListener { view ->
            activity?.runOnUiThread {
                val navController = activity.findNavController(R.id.nav_host_fragment)
                val bundle = bundleOf("preferenceName" to item.tagName)
                navController.navigate(R.id.createPreferenceFragment, bundle)
            }
        }

        val forbiddenTimesRep = turnTimesMapIntoListTimeRep(item.forbiddenTIsettings)
        val forbiddenTimesRecycler = holder.forbiddenTimesRecycler
        forbiddenTimesRecycler.layoutManager = LinearLayoutManager(forbiddenTimesRecycler.context)
        forbiddenTimesRecycler.adapter = PreferenceTimeViewAdapter(forbiddenTimesRep, fm)

        val preferredTimesRep = turnTimesMapIntoListTimeRep(item.preferredTIsettings)
        val preferredTimesRecycler = holder.preferredTimesRecycler
        preferredTimesRecycler.layoutManager = LinearLayoutManager(preferredTimesRecycler.context)
        val preferredTimesAdapter = PreferenceTimeViewAdapter(preferredTimesRep, fm)
        preferredTimesRecycler.adapter = preferredTimesAdapter
    }

    fun updatePreferences(preferences: List<PreferencesLocalDB>) {
        values = preferences
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: PreferancesListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var preferenceName: TextView = binding.preferenceName
        var forbiddenTimesRecycler: RecyclerView = binding.forbiddenTimes
        var preferredTimesRecycler: RecyclerView = binding.preferredTimes
    }


}