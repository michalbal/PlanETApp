package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.UserPreferencesManager
import net.planner.planetapp.adapters.CalendarAccountAdapter
import net.planner.planetapp.databinding.GoogleAccountsFragmentBinding
import net.planner.planetapp.networking.GoogleCalenderCommunicator
import net.planner.planetapp.viewmodels.GoogleAccountsFragmentViewModel

class GoogleAccountsFragment : Fragment() {

    companion object {

        private const val TAG = "GoogleAccountsFragment"

    }
    private lateinit var mBinding: GoogleAccountsFragmentBinding
    private lateinit var viewModel: GoogleAccountsFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = GoogleAccountsFragmentBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(GoogleAccountsFragmentViewModel::class.java)

        // Init Accounts Recycler View
        val accountsRecycler = mBinding.accountsList
        accountsRecycler.layoutManager = LinearLayoutManager(context)
        accountsRecycler.adapter = CalendarAccountAdapter(listOf())

        viewModel.accounts.observe(viewLifecycleOwner, Observer { it?.let {
            Log.d(TAG, "Received accounts from view model")
            val adapter = mBinding.accountsList.adapter as CalendarAccountAdapter
            adapter.updateAccounts(it.toList())
        } })

        mBinding.continueToMoodleButton.setOnClickListener { clicked->
            Log.d(TAG, "Continue was clicked!")
            val adapter = mBinding.accountsList.adapter as CalendarAccountAdapter
            val accounts = adapter.googleAccounts
            Log.d(TAG, "Saving accounts $accounts as google accounts to get events from, saving account ${adapter.mainCalendarName } as Main Calendar")
            GoogleCalenderCommunicator.setCalendarAccounts(accounts)
            if (adapter.mainCalendarName != "") {
                GoogleCalenderCommunicator.setMainCalendar(adapter.mainCalendarName)
            }


            val navController = findNavController()
            if (UserPreferencesManager.didFinishFirstSeq) {
                Log.d(TAG, "Going back to Accounts screen")
                navController.navigateUp()
            } else {
                Log.d(TAG, "Moving on to moodle sign in screen")
                navController.navigate(GoogleAccountsFragmentDirections.actionGoogleAccountsFragmentToMoodleSignInFragment())
            }

        }

        viewModel.updateAccounts()

    }

}