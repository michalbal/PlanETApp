package net.planner.planetapp.fragments

import android.Manifest
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.App
import net.planner.planetapp.R
import net.planner.planetapp.UserPreferencesManager
import net.planner.planetapp.adapters.CalendarAccountAdapter
import net.planner.planetapp.adapters.MoodleCoursesViewAdapter
import net.planner.planetapp.database.local_database.LocalDBManager
import net.planner.planetapp.databinding.AccountsFragmentBinding
import net.planner.planetapp.getHour
import net.planner.planetapp.networking.GoogleCalenderCommunicator
import net.planner.planetapp.viewmodels.AccountsFragmentViewModel

class AccountsFragment : Fragment() {

    companion object {

        private const val TAG = "AccountsFragment"

    }
    private lateinit var mBinding: AccountsFragmentBinding
    private lateinit var viewModel: AccountsFragmentViewModel

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = AccountsFragmentBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(AccountsFragmentViewModel::class.java)

        // Init moodle user
        if (UserPreferencesManager.userMoodleToken != null) {
            mBinding.moodleUsername.text = String.format(App.context.getString(R.string.user_name_moodle), UserPreferencesManager.moodleUserName)
            mBinding.moodleUserButton.text = "Logout"
            mBinding.moodleUserButton.setOnClickListener { view->
                viewModel.moodleLogout()
            }
        } else {
            mBinding.moodleUsername.text = ""
            mBinding.moodleUserButton.text = "Login"
            mBinding.moodleUserButton.setOnClickListener { view->
                val navController = findNavController()
                navController.navigate(AccountsFragmentDirections.actionAccountsFragmentToMoodleSignInFragment())
            }
        }

        // Init Moodle Courses Recycler View
        val coursesRecycler = mBinding.yourCoursesList
        coursesRecycler.layoutManager = LinearLayoutManager(context)
        val moodleAdapter = MoodleCoursesViewAdapter(listOf(), false)
        coursesRecycler.adapter = moodleAdapter
        // Register to get user courses
        LocalDBManager.dbLocalCoursesData.observe(viewLifecycleOwner, Observer { it?.let {
            activity?.runOnUiThread {
                val courseNames = mutableListOf<String>()

                for(value in it) {
                    courseNames.add(value.courseName)
                }
                moodleAdapter.updateCourses(courseNames, true)
            }
        } })

        // Edit courses button
        mBinding.editCoursesButton.setOnClickListener { view->
            val navController = findNavController()
            navController.navigate(AccountsFragmentDirections.actionAccountsFragmentToMoodleCoursesSelectionFragment())
        }

        // Init Accounts Recycler View
        val accountsRecycler = mBinding.googleAccountsList
        accountsRecycler.layoutManager = LinearLayoutManager(context)
        val googleAccountsAdapter = CalendarAccountAdapter(listOf())
        accountsRecycler.adapter = googleAccountsAdapter

        UserPreferencesManager.mainCalendarAccount?.let {
            googleAccountsAdapter.updateMainCalendar(it)
        }

        UserPreferencesManager.calendarAccounts?.let {
            googleAccountsAdapter.updateAccounts(it.toList())
        }

        // This defines what happens after requesting permission for Google Calendar access
        requestPermissionLauncher = this.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            val navController = findNavController()
            Log.d(TAG, "Received response from permission request. isGranted is: $isGranted")
            if (isGranted) {
                // Move to Google Accounts Screen
                Log.d(TAG, "Permission was granted, moving to Google Accounts screen")
                navController.navigate(AccountsFragmentDirections.actionAccountsFragmentToGoogleAccountsFragment())
            } else {
                Log.d(TAG, "Permission was denied, Have no reason to choose calendars")

            }
        }

        mBinding.editGoogleAccountsButton.setOnClickListener { view->
            val navController = findNavController()

            if (GoogleCalenderCommunicator.haveCalendarReadPermissions(App.context)) {
                navController.navigate(AccountsFragmentDirections.actionAccountsFragmentToGoogleAccountsFragment())
            } else {
                // Request permissions  - If granted move to choose accounts screen
                requestPermissionLauncher.apply { launch(Manifest.permission.READ_CALENDAR) }
            }

        }

    }



}