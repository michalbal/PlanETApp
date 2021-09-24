package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.adapters.NextEventViewAdapter
import net.planner.planetapp.databinding.DayFragmentBinding
import net.planner.planetapp.getDayDate
import net.planner.planetapp.planner.PlannerEvent
import net.planner.planetapp.viewmodels.DayFragmentViewModel
import java.util.concurrent.TimeUnit

class DayFragment : Fragment() {

    companion object {
        fun newInstance() = DayFragment()
    }

    private lateinit var viewModel: DayFragmentViewModel
    private lateinit var mBinding: DayFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this).get(DayFragmentViewModel::class.java)

        // @TODO these are for test purposes, to be erased once we have values
        val startTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2)
        val endTime = startTime +  TimeUnit.MINUTES.toMillis(10)
        val todaysEvent = listOf(PlannerEvent("Test Event 1", startTime, endTime),
            PlannerEvent("Test Event 2", startTime, endTime),
            PlannerEvent("Test Event 3", startTime, endTime),
            PlannerEvent("Test Event 4", startTime, endTime))

        mBinding = DayFragmentBinding.inflate(inflater, container, false)

        mBinding.dateText.text = getDayDate(System.currentTimeMillis())

        // Init sub tasks Recycler View
        val subTasksRecycler = mBinding.subTasksList
        subTasksRecycler.layoutManager = LinearLayoutManager(context)
        subTasksRecycler.adapter = NextEventViewAdapter(todaysEvent)

        return mBinding.root
    }
    
    private fun updateDateShown(date: String) {


    }

}