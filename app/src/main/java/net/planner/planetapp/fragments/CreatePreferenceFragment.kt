package net.planner.planetapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.MainActivity
import net.planner.planetapp.R
import net.planner.planetapp.adapters.MoodleCoursesViewAdapter
import net.planner.planetapp.adapters.PreferenceTimeRep
import net.planner.planetapp.adapters.PreferenceTimeViewAdapter
import net.planner.planetapp.database.local_database.LocalDBManager
import net.planner.planetapp.databinding.FragmentCreatePreferenceBinding


class CreatePreferenceFragment : Fragment() {
    companion object {

        private const val TAG = "CreatePreference"

    }

    private lateinit var mBinding: FragmentCreatePreferenceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentCreatePreferenceBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mainActivity = activity as? MainActivity
        mainActivity?.hideBottomNavigation()

        // Priority choice adapter
        val items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
        val adapter = ArrayAdapter(requireContext(), R.layout.priority_list_item, items)
        val editPreference = mBinding.editPreferencePriority.editText as? AutoCompleteTextView
        editPreference?.setAdapter(adapter)

        // Init Forbidden times Recycler View
        val forbiddenTimesRecycler = mBinding.forbiddenTimesList
        forbiddenTimesRecycler.layoutManager = LinearLayoutManager(context)
        val forbiddenTimesAdapter = PreferenceTimeViewAdapter(ArrayList(), parentFragmentManager)
        forbiddenTimesRecycler.adapter = forbiddenTimesAdapter


        // Init preferred times Recycler View
        val preferredTimesRecycler = mBinding.preferredTimesList
        preferredTimesRecycler.layoutManager = LinearLayoutManager(context)
        val preferredTimesAdapter = PreferenceTimeViewAdapter(ArrayList(), parentFragmentManager)
        preferredTimesRecycler.adapter = preferredTimesAdapter

        // Init courses applies to Recycler View
        val coursesRecycler = mBinding.coursesAppliesList
        coursesRecycler.layoutManager = LinearLayoutManager(coursesRecycler.context)
        val courseNames = mutableListOf<String>()
        LocalDBManager.dbLocalCoursesData.value?.let {
            for(value in it) {
                courseNames.add(value.courseName)
            }
        }

        coursesRecycler.adapter = MoodleCoursesViewAdapter(courseNames, true)

        mBinding.addForbiddenButton.setOnClickListener { view->
            forbiddenTimesAdapter.addTime(PreferenceTimeRep())
        }

        mBinding.addPreferredButton.setOnClickListener { view->
            preferredTimesAdapter.addTime(PreferenceTimeRep())
        }

        mBinding.savePreferenceButton.setOnClickListener {
            createNewPreferenceAndReturn()
        }
    }

    fun createNewPreferenceAndReturn() {
        Log.d(TAG, "createNewPreferenceAndReturn")
    }


}