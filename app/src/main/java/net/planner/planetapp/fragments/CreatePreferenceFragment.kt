package net.planner.planetapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.App
import net.planner.planetapp.database.local_database.TaskLocalDB
import net.planner.planetapp.turnTimesMapIntoListTimeRep
import net.planner.planetapp.viewmodels.CreatePreferenceViewModel
import net.planner.planetapp.viewmodels.CreateTaskFragmentViewModel


class CreatePreferenceFragment : Fragment() {
    companion object {

        private const val TAG = "CreatePreference"

    }

    private lateinit var mBinding: FragmentCreatePreferenceBinding
    private lateinit var viewModel: CreatePreferenceViewModel
    private lateinit var moodleAdapter: MoodleCoursesViewAdapter
    private lateinit var forbiddenAdapter: PreferenceTimeViewAdapter
    private lateinit var preferredAdapter: PreferenceTimeViewAdapter
    private val args: CreatePreferenceFragmentArgs by navArgs()
    private var mPreference: PreferencesLocalDB? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        GlobalScope.launch(Dispatchers.IO)  {
            mPreference = args.preferenceName?.let {
                LocalDBManager.getPreference(it)
            }
        }

        mBinding = FragmentCreatePreferenceBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mainActivity = activity as? MainActivity
        mainActivity?.hideBottomNavigation()

        viewModel = ViewModelProvider(this).get(CreatePreferenceViewModel::class.java)

        viewModel.content = CreatePreferenceViewModel.Content(mPreference)
        mBinding.content = viewModel.content

        mBinding.savePreferenceButton.setOnClickListener { view ->
            Log.d(TAG, "Saving preference ${mBinding.editPreferenceName}")
            // Save task
            if (savePreferenceIfPossibleAndReturn()) {
                Toast.makeText(activity, App.context.getText(R.string.saving_preference_message), Toast.LENGTH_SHORT).show()
                viewModel.content.isEditingPreference = false
                val mainActivity = activity as? MainActivity
                mainActivity?.returnBottomNavigation()

                activity?.runOnUiThread {
                    Toast.makeText(activity, App.context.getText(R.string.saving_preference_message), Toast.LENGTH_SHORT).show()
                    val navController = findNavController()
                    navController.navigateUp()
                }
            } else {
                moodleAdapter.updateIsEditable(false)
                preferredAdapter.updateIsEditable(false)
                forbiddenAdapter.updateIsEditable(false)
                Toast.makeText(activity, App.context.getText(R.string.wrong_input_not_saving_message), Toast.LENGTH_LONG).show()
            }
        }

        mBinding.editPreferenceButton.setOnClickListener { view ->
            Log.d(TAG, "Edit enabled")
            viewModel.content.isEditingPreference = true
            createPriorityAdapter()
            moodleAdapter.updateIsEditable(true)
            preferredAdapter.updateIsEditable(true)
            forbiddenAdapter.updateIsEditable(true)
        }


        // Init Forbidden times Recycler View
        val forbiddenTimesRecycler = mBinding.forbiddenTimesList
        forbiddenTimesRecycler.layoutManager = LinearLayoutManager(context)
        forbiddenAdapter = PreferenceTimeViewAdapter(turnTimesMapIntoListTimeRep(viewModel.content.forbiddenTimes), parentFragmentManager, false)
        forbiddenTimesRecycler.adapter = forbiddenAdapter


        // Init preferred times Recycler View
        val preferredTimesRecycler = mBinding.preferredTimesList
        preferredTimesRecycler.layoutManager = LinearLayoutManager(context)
        preferredAdapter = PreferenceTimeViewAdapter(turnTimesMapIntoListTimeRep(viewModel.content.preferredTimes) , parentFragmentManager, false)
        preferredTimesRecycler.adapter = preferredAdapter

        // Init courses applies to Recycler View
        val coursesRecycler = mBinding.coursesAppliesList
        coursesRecycler.layoutManager = LinearLayoutManager(coursesRecycler.context)
        moodleAdapter = MoodleCoursesViewAdapter(viewModel.content.courses.toList(), false)
        coursesRecycler.adapter = moodleAdapter

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

        if (viewModel.content.isEditingPreference) {
            createPriorityAdapter()
        }


        mBinding.addForbiddenButton.setOnClickListener { view->
            forbiddenAdapter.addTime(PreferenceTimeRep())
        }

        mBinding.addPreferredButton.setOnClickListener { view->
            preferredAdapter.addTime(PreferenceTimeRep())
        }

    }

    private fun createPriorityAdapter() {
        // Priority choice adapter
        val items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        val priorityId = items.indexOf(viewModel.content.preferencePriority)
        val adapter = ArrayAdapter(requireContext(), R.layout.priority_list_item, items)
        val editPreference = mBinding.editPreferencePriority.editText as? AutoCompleteTextView
        editPreference?.setAdapter(adapter)
        editPreference?.setText(items[priorityId], false)
    }



    fun savePreferenceIfPossibleAndReturn(): Boolean {
        Log.d(TAG, "createNewPreferenceAndReturn")

        // Create and insert new Preference
        var priority = 5
        try {
            priority = mBinding.editPreferencePriority.editText?.text.toString().toInt()
        } catch(e: Exception) {
            Log.d(TAG, "Could not convert priority")
        }
        val name = mBinding.editPreferenceName.editText?.text.toString() ?: return false
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

        return true


    }


}