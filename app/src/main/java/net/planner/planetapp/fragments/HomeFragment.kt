package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.R
import net.planner.planetapp.adapters.NextEventViewAdapter
import net.planner.planetapp.databinding.HomeFragmentBinding
import net.planner.planetapp.planner.PlannerEvent
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

        viewModel = ViewModelProvider(this).get(HomeFragmentViewModel::class.java)

        mBinding = HomeFragmentBinding.inflate(inflater, container, false)

        val startTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2)
        val endTime = startTime +  TimeUnit.MINUTES.toMillis(10)
        val todaysEvent = listOf<PlannerEvent>(PlannerEvent("Test Event", startTime, endTime))

        // Init Next Events Recycler View
        val nextEventsRecycler = mBinding.nextEventsList
        nextEventsRecycler.layoutManager = LinearLayoutManager(context)
        nextEventsRecycler.adapter = NextEventViewAdapter(todaysEvent)

        return mBinding.root

    }

}