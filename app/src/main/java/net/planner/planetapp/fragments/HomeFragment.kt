package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.planner.planetapp.*
import net.planner.planetapp.adapters.NextEventViewAdapter
import net.planner.planetapp.adapters.TasksViewAdapter
import net.planner.planetapp.database.local_database.LocalDBManager
import net.planner.planetapp.databinding.HomeFragmentBinding
import net.planner.planetapp.planner.PlannerEvent
import net.planner.planetapp.planner.PlannerTask
import net.planner.planetapp.viewmodels.HomeFragmentViewModel
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var mBinding: HomeFragmentBinding

    private lateinit var nextEventAdapter: NextEventViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        val userName = UserPreferencesManager.userName
        val insp = "Every day is a gift, that's why it's called the Present" // TODO have a list of strings

        viewModel = ViewModelProvider(this).get(HomeFragmentViewModel::class.java)

        mBinding = HomeFragmentBinding.inflate(inflater, container, false)

        // Add User information

        mBinding.helloText.text = String.format(App.context.getString(R.string.hello_greeting), userName)
        mBinding.inspText.text = insp

        // Init Next Events Recycler View
        val nextEventsRecycler = mBinding.nextEventsList
        nextEventsRecycler.layoutManager = LinearLayoutManager(context)
        nextEventsRecycler.adapter = NextEventViewAdapter(listOf())

        viewModel.eventsToDisplay.observe(viewLifecycleOwner, Observer { it?.let {
            val adapter = mBinding.nextEventsList.adapter as NextEventViewAdapter
            adapter.updateEvents(it.toList())
        } })


        // Init Tasks list Recycler View
        val tasksRecycler = mBinding.tasksList
        tasksRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        val mainActivity = activity as? MainActivity
        tasksRecycler.adapter = TasksViewAdapter(listOf(), activity = mainActivity)

        LocalDBManager.dbLocalTasksData.observe(viewLifecycleOwner, Observer { it?.let {
            val adapter = mBinding.tasksList.adapter as TasksViewAdapter
            adapter.updateTasks(it)
        } })

        return mBinding.root

    }

}