package net.planner.planetapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.navigateUp
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.android.synthetic.main.fragment_create_task.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.*
import net.planner.planetapp.database.local_database.LocalDBManager
import net.planner.planetapp.database.local_database.PreferencesLocalDB
import net.planner.planetapp.database.local_database.TaskLocalDB
import net.planner.planetapp.databinding.FragmentCreateTaskBinding
import net.planner.planetapp.planner.PlannerTask
import net.planner.planetapp.viewmodels.CreateTaskFragmentViewModel
import java.util.concurrent.TimeUnit


class CreateTaskFragment : Fragment() {

    companion object {

        private const val TAG = "CreateTaskFragment"

    }

    private lateinit var viewModel: CreateTaskFragmentViewModel
    private lateinit var mBinding: FragmentCreateTaskBinding
    private val args: CreateTaskFragmentArgs by navArgs()
    private var mTask:TaskLocalDB? = null
    private var preferences: List<PreferencesLocalDB> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                mTask = args.taskId?.let {
                    LocalDBManager.getTask(it)
                }
            }
        }

        mBinding = FragmentCreateTaskBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(this).get(CreateTaskFragmentViewModel::class.java)
        val deadlineDateStart = args.deadlineDateStart

        viewModel.content = CreateTaskFragmentViewModel.Content(mTask)
        mBinding.content = viewModel.content


        mBinding.editDeadlineDate.text = getDayDate(deadlineDateStart)

        mBinding.saveTaskButton.setOnClickListener { view ->
            Log.d(TAG, "Saving task ${mBinding.editTaskTitle}")
            // Save task
            saveInputIfPossible()
            viewModel.content.isEditingTask = false
        }

        mBinding.editTaskButton.setOnClickListener { view ->
            Log.d(TAG, "Edit enabled")
            viewModel.content.isEditingTask = true
            createPreferenceAdapter()
            createPriorityAdapter()
        }

        // Init deadline date picker
        edit_deadline_date.setOnClickListener { view->

            // Makes only dates from today forward selectable.
            val constraintsBuilder =
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())

            val dateMillis: Long =
                try {
                    getMillisFromDate(mBinding.editDeadlineDate.text.toString())
                } catch (e: Exception) {
                    deadlineDateStart
                } ?: deadlineDateStart


            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select deadline date")
                    .setSelection(dateMillis)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()

            datePicker.addOnPositiveButtonClickListener {
                mBinding.editDeadlineDate.text = getDayDate(it)
            }

            datePicker.show(parentFragmentManager, null)
        }

        // Init deadline time picker
        mBinding.editDeadlineTime.setOnClickListener { view ->
            // Show time picker dialog
            val splitted = mBinding.editDeadlineTime.text.split(":")
            val hour = splitted[0].trim().toInt()
            val minutes = splitted[1].trim().toInt()

            val picker =
                MaterialTimePicker.Builder()
                    .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(hour)
                    .setMinute(minutes)
                    .build()

            picker.addOnPositiveButtonClickListener {
                mBinding.editDeadlineTime.text = createTimeString(picker.hour, picker.minute)
            }

            picker.show(parentFragmentManager, "Choose deadline time:")
        }

        // Add preference button
        mBinding.addPreferenceButton.setOnClickListener { view ->
            activity?.runOnUiThread {
                val navController = findNavController()
                navController.navigate(CreateTaskFragmentDirections.actionCreateTaskFragmentToCreatePreferenceFragment(null))
            }
        }

        // Fill preferences adapter
        LocalDBManager.dbLocalPreferencesData.observe(viewLifecycleOwner, Observer {
            it?.let {
                preferences = it

                if (viewModel.content.isEditingTask) {
                    createPreferenceAdapter()
                }
            }
        })

        if (viewModel.content.isEditingTask) {
            createPreferenceAdapter()
            createPriorityAdapter()
        } else {
            mBinding.editTaskPreference.editText?.setText(viewModel.content.taskPreferenceName)
        }


        mBinding.editEstimatedDuration.editText?.doOnTextChanged { inputText, _, _, _ ->
            // Respond to input text change
            if (!isAvgTaskInputValid(inputText.toString())) {
                Log.d(TAG, "Task duration Hours with wrong value! value is $inputText")
                // Set error text
                mBinding.editEstimatedDuration.error = getString(R.string.error_task_duration)
            } else {
                // Clear error text
                mBinding.editEstimatedDuration.error = null
            }
        }


        mBinding.editPreferredSessionTask.editText?.doOnTextChanged { inputText, _, _, _ ->
            val avgTaskTime: Double = try {
                mBinding.editEstimatedDuration.editText.toString().toDouble()
            } catch (e: Exception) {
                UserPreferencesManager.avgTaskDurationMinutes.toDouble() / 60
            }

            // Respond to input text change
            if (!isPreferredTimeInputValid(inputText.toString(), avgTaskTime)) {
                Log.d(TAG, "isPreferredTimeInputValid: preferredSessionTimeHours with wrong value! value is $inputText")
                // Set error text
                mBinding.editPreferredSessionTask.error = getString(R.string.error_preferred_session)
            } else {
                // Clear error text
                mBinding.editPreferredSessionTask.error = null
            }
        }

        mBinding.editMaxNumSessionsTask.editText?.doOnTextChanged { inputText, _, _, _ ->
            // Respond to input text change
            try {
                val maxDivisionsNum = inputText.toString().toDouble()
                if(maxDivisionsNum < 0 || maxDivisionsNum > 100) {
                    mBinding.editMaxNumSessionsTask.error = getString(R.string.error_max_divisions)
                } else {
                    Log.d(TAG, " maxDivisionsNum OK value is $maxDivisionsNum")
                    mBinding.editMaxNumSessionsTask.error = null
                }
            } catch(e: Exception) {
                mBinding.editMaxNumSessionsTask.error = getString(R.string.error_max_divisions)
            }
        }

        mBinding.calculateTaskButton.setOnClickListener { view ->

            Log.d(TAG, "calculateTaskButton: pressd Creating and showing the dialog")
            val dialog = activity?.let {
                AlertDialog.Builder(it)
                    .setMessage(R.string.recalculate_warning_message)
                    .setTitle(R.string.are_you_sure_dialog_title)
                    .setNegativeButton(android.R.string.cancel){ dialog, _ ->
                        dialog.cancel()
                    }
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->
                        viewModel.calculateTask(args.taskId)
                        dialog.cancel()
                    }
                    .create()
                    .show()
            }
        }


    }

    private fun createPriorityAdapter() {
        // Priority choice adapter
        val items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        val priorityId = items.indexOf(viewModel.content.taskPriority)
        val adapter = ArrayAdapter(requireContext(), R.layout.priority_list_item, items)
        val editPreference = mBinding.editTaskPriority.editText as? AutoCompleteTextView
        editPreference?.setAdapter(adapter)
        editPreference?.setText(items[priorityId], false)
    }


    private fun createPreferenceAdapter() {
        // Preference choice adapter
        val adapter = ArrayAdapter(requireContext(), R.layout.priority_list_item, preferences.map { it.tagName })
        val editPreference = mBinding.editTaskPreference.editText as? AutoCompleteTextView
        editPreference?.setAdapter(adapter)
        editPreference?.setText(viewModel.content.taskPreferenceName, false)
    }

    private fun saveInputIfPossible(): Boolean {
        try {

            val taskTitle = mBinding.editTaskTitle.editText?.text.toString()
            if (taskTitle == "" || taskTitle == "null") {
                return false
            }

            val deadlineDate = mBinding.editDeadlineDate.text
            val deadlineTime = mBinding.editDeadlineTime.text

            val deadline: Long = getMillisFromDateAndTime(deadlineDate.toString() + " " + deadlineTime) ?: return false
            val preferenceName = viewModel.content.taskPreferenceName
            val priority = viewModel.content.taskPriority.toInt()


            val avgTaskHours = mBinding.editEstimatedDuration.editText?.text.toString().toDouble().toLong()
            val avgTaskMinutes = TimeUnit.HOURS.toMinutes(avgTaskHours)

            val preferredSessionTimeHours = mBinding.editPreferredSessionTask.editText?.text.toString().toDouble().toLong()
            val preferredSessionTimeMinutes = TimeUnit.HOURS.toMinutes(preferredSessionTimeHours)

            val maxDivisionsNum = mBinding.editMaxNumSessionsTask.editText?.text.toString().toDouble().toLong()

            val taskId = mTask?.taskId ?: System.currentTimeMillis()
            val taskUpdated = TaskLocalDB(taskId.toString(), taskTitle, tag = preferenceName,
                deadline = deadline, priority = priority, durationInMinutes = avgTaskMinutes.toInt(),
                maxSessionTimeInMinutes = preferredSessionTimeMinutes.toInt(),
                maxDivisionsNumber = maxDivisionsNum.toInt(), subtaskDates = listOf())

            viewModel.saveTask(taskUpdated)

            if(mTask == null) {
                activity?.runOnUiThread {
                    val navController = findNavController()
                    navController.navigateUp()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "saveInputIfPossible: Received exception ${e.message}")
            return false
        }
        return true
    }

}