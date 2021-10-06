package net.planner.planetapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.MainActivity
import net.planner.planetapp.R
import net.planner.planetapp.adapters.MoodleCoursesViewAdapter
import net.planner.planetapp.adapters.PreferenceTimeRep
import net.planner.planetapp.adapters.PreferenceTimeViewAdapter
import net.planner.planetapp.database.local_database.LocalDBManager
import net.planner.planetapp.database.local_database.PreferencesLocalDB
import net.planner.planetapp.databinding.FragmentCreatePreferenceBinding
import net.planner.planetapp.planner.PlannerTag
import net.planner.planetapp.planner.TasksManager
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
        val items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
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
        coursesRecycler.adapter = MoodleCoursesViewAdapter(listOf(), true)

        LocalDBManager.dbLocalCoursesData.observe(viewLifecycleOwner, Observer { it?.let {
            activity?.runOnUiThread {
                val adapter = mBinding.coursesAppliesList.adapter as MoodleCoursesViewAdapter
                val courseNames = mutableListOf<String>()

                for(value in it) {
                    Log.d(TAG, "Found course $value")
                    courseNames.add(value.courseName)
                }
                adapter.updateCourses(courseNames, false)
            }
        } })

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

        // Create and insert new Preference
        var priority = 5
        try {
            priority = mBinding.editPreferencePriority.editText?.text.toString().toInt()
        } catch(e: Exception) {
            Log.d(TAG, "Could not convert priority")
        }
        val name = mBinding.editPreferenceName.editText?.text.toString() ?: return // TODO should show message here
        val forbiddenTimesAdapter = mBinding.forbiddenTimesList.adapter as PreferenceTimeViewAdapter
        val forbiddenTimes = forbiddenTimesAdapter.getTimesPreferenceFormat()
        val preferredTimesAdapter = mBinding.preferredTimesList.adapter as PreferenceTimeViewAdapter
        val preferredTimes = preferredTimesAdapter.getTimesPreferenceFormat()
        val coursesAdapter = mBinding.coursesAppliesList.adapter as MoodleCoursesViewAdapter
        val courses = coursesAdapter.courseIds

        GlobalScope.launch {
            withContext(Dispatchers.IO) {

                val preference = PlannerTag(name, priority, forbiddenTimes, preferredTimes)

                // Save preference in local db
                val preferenceLocal = PreferencesLocalDB(name, priority, preferredTimes, forbiddenTimes, courses)

                LocalDBManager.insertOrUpdatePreference(preferenceLocal)

                //Save preference to firebase db
                TasksManager.getInstance().addPreferenceTag(preference, true)
            }
        }

        val mainActivity = activity as? MainActivity
        mainActivity?.returnBottomNavigation()

        activity?.runOnUiThread {
            val navController = findNavController()
            navController.navigateUp()
        }


    }


}