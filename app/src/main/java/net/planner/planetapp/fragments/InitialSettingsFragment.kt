package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import net.planner.planetapp.*
import net.planner.planetapp.databinding.InitialSettingsFragmentBinding
import net.planner.planetapp.viewmodels.InitialSettingsFragmentViewModel
import java.util.concurrent.TimeUnit

class InitialSettingsFragment : Fragment() {

    companion object {

        private const val TAG = "InitialSettingsFragment"

    }

    private lateinit var viewModel: InitialSettingsFragmentViewModel
    private lateinit var mBinding: InitialSettingsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = InitialSettingsFragmentBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(InitialSettingsFragmentViewModel::class.java)

        mBinding.editAverageTaskDuration.editText?.doOnTextChanged { inputText, _, _, _ ->
            // Respond to input text change
            if (!isAvgTaskInputValid(inputText.toString())) {
                Log.d(TAG, "testInputIntegrity: Avg Hours with wrong value! value is $inputText")
                // Set error text
                mBinding.editAverageTaskDuration.error = getString(R.string.error_task_duration)
            } else {
            // Clear error text
            mBinding.editAverageTaskDuration.error = null
            }
        }

        mBinding.editPreferredSession.editText?.doOnTextChanged { inputText, _, _, _ ->
            val avgTaskTime: Double = try {
                mBinding.editAverageTaskDuration.editText.toString().toDouble()
            } catch (e: Exception) {
                UserPreferencesManager.avgTaskDurationMinutes.toDouble() / 60
            }

            // Respond to input text change
            if (!isPreferredTimeInputValid(inputText.toString(), avgTaskTime)) {
                Log.d(TAG, "isPreferredTimeInputValid: preferredSessionTimeHours with wrong value! value is $inputText")
                // Set error text
                mBinding.editPreferredSession.error = getString(R.string.error_preferred_session)
            } else {
                // Clear error text
                mBinding.editPreferredSession.error = null
            }
        }

        mBinding.editMinSession.editText?.doOnTextChanged { inputText, _, _, _ ->
            // Respond to input text change
            try {
                val spaceBetweenSessions = inputText.toString().toDouble()
                Log.d(TAG, "testInputIntegrity: spaceBetweenSessions OK value is $spaceBetweenSessions")
                mBinding.editMinSession.error = null
            } catch(e: Exception) {
                mBinding.editMinSession.error = getString(R.string.error_min_time)
            }
        }

        mBinding.saveInitislSettingsButton.setOnClickListener { view ->

            Log.d(TAG, "Saving settings")

            if (!testInputIntegrity()) {
                Log.d(TAG, "One of the values is incorrect, cannot continue")
                mBinding.warningInstructions.visibility = VISIBLE
                return@setOnClickListener
            }

            UserPreferencesManager.didFinishFirstSeq = true

            Log.d(TAG, "Moving on to Home Screen start retrieving tasks from moodle")
            viewModel.startGettingTasksFromMoodle()

            val mainActivity = activity as MainActivity
            mainActivity?.let {
                it.runOnUiThread {
                    it.returnBottomNavigation()
                    val navController = findNavController()
                    navController.navigate(InitialSettingsFragmentDirections.actionInitialSettingsFragmentToHomeFragment())
                }
            }
        }

    }

    private fun testInputIntegrity(): Boolean {
        try {
            Log.d(TAG, "testInputIntegrity")

            val avgTaskInput = mBinding.editAverageTaskDuration.editText?.text.toString()
            val preferredSessionTimeInput = mBinding.editPreferredSession.editText?.text.toString()
            val spaceBetweenSessions = mBinding.editMinSession.editText?.text.toString().toDouble().toLong()

            if (!isAvgTaskInputValid(avgTaskInput) || !isPreferredTimeInputValid(preferredSessionTimeInput, avgTaskInput.toDouble())) {
                return false
            }

            val avgTaskHours = avgTaskInput.toDouble().toLong()
            val avgTaskMinutes = TimeUnit.HOURS.toMinutes(avgTaskHours)

            // Can save avgTask
            UserPreferencesManager.avgTaskDurationMinutes = avgTaskMinutes

            val preferredSessionTimeHours = preferredSessionTimeInput.toDouble().toLong()
            val preferredSessionTimeMinutes = TimeUnit.HOURS.toMinutes(preferredSessionTimeHours)

            // Can save preferred session
            UserPreferencesManager.preferredSessionTime = preferredSessionTimeMinutes

            // Can save min session
            UserPreferencesManager.spaceBetweenEventsMinutes = spaceBetweenSessions


        } catch (e: Exception) {
            Log.e(TAG, "Received exception ${e.message}")
            return false
        }
        return true
    }



}