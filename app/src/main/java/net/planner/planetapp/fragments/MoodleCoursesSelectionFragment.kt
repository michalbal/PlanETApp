package net.planner.planetapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.adapters.MoodleCoursesViewAdapter
import net.planner.planetapp.databinding.FragmentMoodleCoursesSelectionBinding
import net.planner.planetapp.viewmodels.MoodleCoursesSelectionFragmentViewModel


class MoodleCoursesSelectionFragment : Fragment() {
    companion object {

        private const val TAG = "MoodleSelectionFragment"

    }

    private lateinit var mBinding: FragmentMoodleCoursesSelectionBinding
    private lateinit var viewModel: MoodleCoursesSelectionFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentMoodleCoursesSelectionBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(MoodleCoursesSelectionFragmentViewModel::class.java)

        // Init Courses Recycler View
        val coursesRecycler = mBinding.coursesList
        coursesRecycler.layoutManager = LinearLayoutManager(context)
        coursesRecycler.adapter = MoodleCoursesViewAdapter(listOf(), true)

        viewModel.courses.observe(viewLifecycleOwner, Observer { it?.let {
            Log.d(TAG, "Received courses from ViewModel. Courses are $it")
            val adapter = mBinding.coursesList.adapter as MoodleCoursesViewAdapter
            activity?.runOnUiThread {
                adapter.updateCourses(it.toList())
            }
        } })

        mBinding.continueToSettingsButton.setOnClickListener { clicked->
            Log.d(TAG, "Clicked on Continue!")
            val adapter = mBinding.coursesList.adapter as MoodleCoursesViewAdapter
            val courses = adapter.courseIds
            // TODO maybe also get the courses not chosen here? need to see if maybe only the ones we have is enough
            viewModel.saveChosenCourses(courses)

            activity?.runOnUiThread {
                val navController = findNavController()
                navController.navigate(MoodleCoursesSelectionFragmentDirections.actionMoodleCoursesSelectionFragmentToInitialSettingsFragment())
            }
        }

        viewModel.updateCourses()

    }

}