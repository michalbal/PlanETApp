package net.planner.planetapp.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.App
import net.planner.planetapp.R
import net.planner.planetapp.adapters.NextEventViewAdapter
import net.planner.planetapp.databinding.DayFragmentBinding
import net.planner.planetapp.getDayDate
import net.planner.planetapp.planner.PlannerEvent
import net.planner.planetapp.viewmodels.DayFragmentViewModel
import java.util.concurrent.TimeUnit

class DayFragment : Fragment() {

    companion object {

        private const val TAG = "DayFragment"

        private val ONE_DAY_MOVE = TimeUnit.DAYS.toMillis(1)
    }


    private lateinit var viewModel: DayFragmentViewModel
    private lateinit var mBinding: DayFragmentBinding
    // TODO keep the adapter also

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

        // Subscribe to updates to event list
        viewModel.eventsToDisplay.observe(viewLifecycleOwner, Observer { it?.let {
            val adapter = mBinding.subTasksList.adapter as NextEventViewAdapter
            adapter.updateEvents(it.toList())
        } })

        // Setting button reactions
        mBinding.rightImage.setOnClickListener { button ->
            val nextDayDate = getDayDate(System.currentTimeMillis() + ONE_DAY_MOVE)
            updateDateShown(nextDayDate)
        }

        mBinding.leftImage.setOnClickListener { button ->
            val lastDayDate = getDayDate(System.currentTimeMillis() - ONE_DAY_MOVE)
            updateDateShown(lastDayDate)
        }



        return mBinding.root
    }
    
    private fun updateDateShown(date: String) = when {
        ContextCompat.checkSelfPermission(
            App.context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED -> {
            updateDateWithPermission(date)
        }

        shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR) -> {
            val message = App.context.getString(R.string.request_read_events_message)
            val title = App.context.getString(R.string.request_read_permission)
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                    android.R.string.ok,
                ) { dialog, _ ->
                    dialog.cancel()
                    updateDateWithPermission(date)
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                    updateDateNoPermission(date)
                }
                .create()
                .show()
        }

        else -> {
            val requestPermissionLauncher = this.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    updateDateWithPermission(date)
                } else {
                    updateDateNoPermission(date)
                }
            }.apply { launch(Manifest.permission.READ_CALENDAR) }
        }
    }

    private fun updateDateWithPermission(date: String) {
        mBinding.dateText.text = date
        viewModel.updateEventsForDay(date)
    }

    private fun updateDateNoPermission(date: String) {
        mBinding.dateText.text = date
        val adapter = mBinding.subTasksList.adapter as NextEventViewAdapter
        adapter.updateEvents(listOf())
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

}