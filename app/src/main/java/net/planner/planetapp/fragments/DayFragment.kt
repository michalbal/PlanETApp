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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.fragment_create_task.*
import net.planner.planetapp.*
import net.planner.planetapp.adapters.NextEventViewAdapter
import net.planner.planetapp.databinding.DayFragmentBinding
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
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
//    private var date = getDayDate(System.currentTimeMillis())
    private var date = getDayDate(getTodayTimeMillis())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DayFragmentBinding.inflate(inflater, container, false)

        requestPermissionLauncher = this.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                updateDateWithPermission(date)
            } else {
                updateDateNoPermission(date)
            }
        }

        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(DayFragmentViewModel::class.java)

        mBinding.dateText.text = getDayDate(getTodayTimeMillis())

        // Init sub tasks Recycler View
        val subTasksRecycler = mBinding.subTasksList
        subTasksRecycler.layoutManager = LinearLayoutManager(context)
        subTasksRecycler.adapter = NextEventViewAdapter(listOf())

        // Subscribe to updates to event list
        viewModel.eventsToDisplay.observe(viewLifecycleOwner, Observer { it?.let {
            mBinding.noEventsFoundText.visibility = View.INVISIBLE
            val adapter = mBinding.subTasksList.adapter as NextEventViewAdapter
            adapter.updateEvents(it.toList())
        } })

        // Setting button reactions
        mBinding.rightImage.setOnClickListener { button ->
            val currentDateMillis = getMillisFromDate(mBinding.dateText.text.toString()) ?: getTodayTimeMillis()
            val nextDayDate = getDayDate(currentDateMillis + ONE_DAY_MOVE)
            updateDateShown(nextDayDate)
        }

        mBinding.leftImage.setOnClickListener { button ->
            val currentDateMillis = getMillisFromDate(mBinding.dateText.text.toString()) ?: getTodayTimeMillis()
            val lastDayDate = getDayDate(currentDateMillis- ONE_DAY_MOVE)
            updateDateShown(lastDayDate)
        }

        // Init date picker
        mBinding.chooseDayImage.setOnClickListener { view->

            val dateMillis: Long =
                try {
                    getMillisFromDate(mBinding.dateText.text.toString())
                } catch (e: Exception) {
                    getTodayTimeMillis()
                } ?: getTodayTimeMillis()


            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(dateMillis)
                    .build()

            datePicker.addOnPositiveButtonClickListener {
                updateDateShown(getDayDate(it))
            }

            datePicker.show(parentFragmentManager, null)
        }

        // Init add task button
        mBinding.addTaskButton.setOnClickListener { view ->
            activity?.runOnUiThread {
                val dateMillis: Long =
                    try {
                        getMillisFromDate(mBinding.dateText.text.toString())
                    } catch (e: Exception) {
                        getTodayTimeMillis()
                    } ?: getTodayTimeMillis()
                val navController = findNavController()
                navController.navigate(DayFragmentDirections.actionDayFragmentToCreateTaskFragment(dateMillis, null))
            }
        }

        updateDateShown(getDayDate(getTodayTimeMillis()))
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
            // TODO add runOnUIThread?
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
            requestPermissionLauncher.apply { launch(Manifest.permission.READ_CALENDAR) }
        }
    }

    private fun updateDateWithPermission(dateUpdated: String) {
        val adapter = mBinding.subTasksList.adapter as NextEventViewAdapter
        adapter.updateEvents(listOf())
        mBinding.noEventsFoundText.visibility = View.VISIBLE
        date = dateUpdated
        mBinding.dateText.text = dateUpdated
        viewModel.updateEventsForDay(dateUpdated)
    }

    private fun updateDateNoPermission(dateUpdated: String) {
        mBinding.noEventsFoundText.visibility = View.VISIBLE
        date = dateUpdated
        mBinding.dateText.text = dateUpdated
        val adapter = mBinding.subTasksList.adapter as NextEventViewAdapter
        adapter.updateEvents(listOf())
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

}