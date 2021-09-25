package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.planner.planetapp.App
import net.planner.planetapp.R
import net.planner.planetapp.adapters.NextEventViewAdapter
import net.planner.planetapp.adapters.TasksViewAdapter
import net.planner.planetapp.databinding.HomeFragmentBinding
import net.planner.planetapp.getDate
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

        // @TODO these are for test purposes, to be erased once we have values
        val userName = "Michal"
        val insp = "Every day is a gift, that's why it's called the Present"

        val startTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2)
        val endTime = startTime +  TimeUnit.MINUTES.toMillis(10)
        val todaysEvent = listOf(PlannerEvent("Test Event 1", startTime, endTime), PlannerEvent("Test Event 2", startTime, endTime))
        val testTask = PlannerTask("Test Task 1", endTime, 10)
        val testTaskSecond = PlannerTask("Test Task 2", endTime, 30)
        val tasks = listOf(testTask, testTaskSecond)

        //-----------------------------------------------------------------------

        viewModel = ViewModelProvider(this).get(HomeFragmentViewModel::class.java)

        mBinding = HomeFragmentBinding.inflate(inflater, container, false)

        // Add User information

        mBinding.helloText.text = String.format(App.context.getString(R.string.hello_greeting), userName)
        mBinding.inspText.text = insp

        // Init Next Events Recycler View
        val nextEventsRecycler = mBinding.nextEventsList
        nextEventsRecycler.layoutManager = LinearLayoutManager(context)
        nextEventsRecycler.adapter = NextEventViewAdapter(todaysEvent)

        // Init Tasks list Recycler View

        val tasksRecycler = mBinding.tasksList
        tasksRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        tasksRecycler.adapter = TasksViewAdapter(tasks)
        return mBinding.root

    }

}