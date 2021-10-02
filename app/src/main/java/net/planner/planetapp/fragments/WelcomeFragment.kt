package net.planner.planetapp.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import net.planner.planetapp.UserPreferencesManager
import net.planner.planetapp.databinding.WelcomeFragmentBinding

class WelcomeFragment : Fragment() {

    companion object {

        private const val TAG = "WelcomeFragment"

    }

    private lateinit var mBinding: WelcomeFragmentBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = WelcomeFragmentBinding.inflate(inflater, container, false)

        // This defines what happens after requesting permission for Google Calendar access
        requestPermissionLauncher = this.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            val navController = findNavController()
            Log.d(TAG, "Received response from permission request. isGranted is: $isGranted")
            if (isGranted) {
                // Move to Google Accounts Screen
                Log.d(TAG, "Permission was granted, moving to Google Accounts screen")
                navController.navigate(WelcomeFragmentDirections.actionWelcomeFragmentToGoogleAccountsFragment())
            } else {
                // Move to Moodle Screen
                Log.d(TAG, "Permission was denied, moving to Moodle login screen")
                UserPreferencesManager.mainCalendarAccount = "No Account"
                navController.navigate(WelcomeFragmentDirections.actionWelcomeFragmentToMoodleSignInFragment())
            }
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mBinding.continueButton.setOnClickListener { button ->
            // Save name
            UserPreferencesManager.userName = mBinding.nameEdt.editText?.text.toString() ?: ""

            // Request permissions  - If granted move to next screen
            requestPermissionLauncher.apply { launch(Manifest.permission.READ_CALENDAR) }

        }

    }


}