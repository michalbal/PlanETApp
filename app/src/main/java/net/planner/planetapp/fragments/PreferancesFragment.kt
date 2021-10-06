package net.planner.planetapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import net.planner.planetapp.MainActivity
import net.planner.planetapp.adapters.PreferenceListItemAdapter
import net.planner.planetapp.database.local_database.LocalDBManager
import net.planner.planetapp.databinding.FragmentPreferancesListBinding


class PreferancesFragment : Fragment() {

    companion object {

        private const val TAG = "PreferancesFragment"

    }

    private lateinit var mBinding: FragmentPreferancesListBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentPreferancesListBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val preferences = LocalDBManager.dbLocalPreferencesData.value ?: listOf()

        // Init Preferences Recycler View
        val preferencesRecycler = mBinding.preferancesList
        preferencesRecycler.layoutManager = LinearLayoutManager(context)
        preferencesRecycler.adapter = PreferenceListItemAdapter(preferences, parentFragmentManager ,activity as? MainActivity)

        LocalDBManager.dbLocalPreferencesData.observe(viewLifecycleOwner, Observer { it?.let {
            activity?.runOnUiThread {
                val adapter = mBinding.preferancesList.adapter as PreferenceListItemAdapter
                adapter.updatePreferences(it.toList())
            }
        } })

        mBinding.addPreferenceButton.setOnClickListener { view ->
            activity?.runOnUiThread {
                val navController = findNavController()
                navController.navigate(PreferancesFragmentDirections.actionPreferancesFragmentToCreatePreferenceFragment(null))
            }
        }


    }

}