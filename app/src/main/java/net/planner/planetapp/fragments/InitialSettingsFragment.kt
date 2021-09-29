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
            Log.d(TAG, "testInputIntegrity")
            val avgTaskHours = mBinding.editAverageTaskDuration.text.toString().toDouble().toLong()
            val avgTaskMinutes = TimeUnit.HOURS.toMinutes(avgTaskHours)
            if (avgTaskMinutes < 30 || avgTaskMinutes > 6000) {
                Log.d(TAG, "testInputIntegrity: Avg Hours with wrong value! value is $avgTaskHours")
                return false
            }

            Log.d(TAG, "testInputIntegrity: Avg Hours OK value is $avgTaskHours")
            // Can save avgTask
            UserPreferencesManager.avgTaskDurationMinutes = avgTaskMinutes

            val preferredSessionTimeHours = mBinding.editPreferredSession.text.toString().toDouble().toLong()
            val preferredSessionTimeMinutes = TimeUnit.HOURS.toMinutes(preferredSessionTimeHours)
            if (preferredSessionTimeMinutes < 30 || preferredSessionTimeMinutes >= avgTaskMinutes) {
                Log.d(TAG, "testInputIntegrity: preferredSessionTimeHours with wrong value! value is $preferredSessionTimeHours")
                return false
            }

            Log.d(TAG, "testInputIntegrity: preferredSessionTimeHours OK value is $preferredSessionTimeHours")

            // Can save min session
            UserPreferencesManager.preferredSessionTime = preferredSessionTimeMinutes

            val spaceBetweenSessions = mBinding.editMinSession.text.toString().toDouble().toLong()
            Log.d(TAG, "testInputIntegrity: spaceBetweenSessions OK value is $spaceBetweenSessions")

            // Can save preferred session
            UserPreferencesManager.spaceBetweenEventsMinutes = spaceBetweenSessions


        } catch (e: Exception) {
            Log.e(TAG, "Received exception ${e.message}")
            return false
        }
        return true
    }



}