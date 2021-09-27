package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
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
            val adapter = mBinding.accountsList.adapter as CalendarAccountAdapter
            adapter.updateAccounts(it.toList())
        } })

        mBinding.continueToMoodleButton.setOnClickListener { clicked->
            val adapter = mBinding.accountsList.adapter as CalendarAccountAdapter

        }

        viewModel.updateAccounts()

    }

}