package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import net.planner.planetapp.MainActivity
import net.planner.planetapp.UserPreferencesManager
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

        mBinding.saveInitislSettingsButton.setOnClickListener { view ->

            Log.d(TAG, "Saving settings")

            if (!testInputIntegrity()) {
                Log.d(TAG, "One of the values is incorrect, cannot continue")
                mBinding.warningInstructions.visibility = VISIBLE
                return@setOnClickListener
            }

            Log.d(TAG, "Moving on to Home Screen")
            val mainActivity = activity as MainActivity
            mainActivity?.returnBottomNavigation()
            val navController = findNavController()
            navController.navigate(InitialSettingsFragmentDirections.actionInitialSettingsFragmentToHomeFragment())
        }

    }

    private fun testInputIntegrity(): Boolean {
        try {

            val avgTaskHours = mBinding.editAverageTaskDuration.text.toString().toLong()
            val avgTaskMinutes = TimeUnit.HOURS.toMinutes(avgTaskHours)
            if (avgTaskMinutes < 30 || avgTaskMinutes > 6000) {
                return false
            }

            // Can save avgTask
            UserPreferencesManager.avgTaskDurationMinutes = avgTaskMinutes

            val preferredSessionTimeHours = mBinding.editMinSession.text.toString().toLong()
            val preferredSessionTimeMinutes = TimeUnit.HOURS.toMinutes(preferredSessionTimeHours)
            if (preferredSessionTimeMinutes < 30 || preferredSessionTimeMinutes >= avgTaskMinutes) {
                return false
            }

            // Can save min session
            UserPreferencesManager.preferredSessionTime = preferredSessionTimeMinutes

            val spaceBetweenSessions = mBinding.editMinSession.text.toString().toLong()

            // Can save preferred session
            UserPreferencesManager.spaceBetweenEventsMinutes = spaceBetweenSessions


        } catch (e: Exception) {
            return false
        }
        return true
    }



}