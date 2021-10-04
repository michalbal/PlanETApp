package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import net.planner.planetapp.MainActivity
import net.planner.planetapp.R
import net.planner.planetapp.UserPreferencesManager
import net.planner.planetapp.databinding.InitialSettingsFragmentBinding
import net.planner.planetapp.databinding.SettingsFragmentBinding
import net.planner.planetapp.viewmodels.InitialSettingsFragmentViewModel
import net.planner.planetapp.viewmodels.SettingsFragmentViewModel
import java.util.concurrent.TimeUnit

class SettingsFragment : Fragment() {

    companion object {

        private const val TAG = "InitialSettingsFragment"

    }

    private lateinit var viewModel: SettingsFragmentViewModel
    private lateinit var mBinding: SettingsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = SettingsFragmentBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel = ViewModelProvider(this).get(SettingsFragmentViewModel::class.java)
        mBinding.content = viewModel.content

        mBinding.saveSettingsButton.setOnClickListener { view ->
            Log.d(TAG, "Saving settings")
            // Save settings
            saveInputIfPossible()
            viewModel.content.isEditing = false
        }

        mBinding.editSettingsButton.setOnClickListener { view ->
            Log.d(TAG, "Edit enabled")
            viewModel.content.isEditing = true
        }
        // TODO add input validation here


    }

    private fun saveInputIfPossible(): Boolean {
        UserPreferencesManager.userName = mBinding.editUserName.editText?.text.toString()
        try {
            val avgTaskHours = mBinding.editAverageTaskDurationSettings.editText?.text.toString().toDouble().toLong()
            val avgTaskMinutes = TimeUnit.HOURS.toMinutes(avgTaskHours)
            UserPreferencesManager.avgTaskDurationMinutes = avgTaskMinutes

            val preferredSessionTimeHours = mBinding.editPreferredSessionSettings.editText?.text.toString().toDouble().toLong()
            val preferredSessionTimeMinutes = TimeUnit.HOURS.toMinutes(preferredSessionTimeHours)
            UserPreferencesManager.preferredSessionTime = preferredSessionTimeMinutes

            val spaceBetweenSessions = mBinding.editMinSessionSettings.editText?.text.toString().toDouble().toLong()
            UserPreferencesManager.spaceBetweenEventsMinutes = spaceBetweenSessions

        } catch (e: Exception) {
            Log.e(TAG, "saveInputIfPossible: Received exception ${e.message}")
            return false
        }
        return true
    }

}