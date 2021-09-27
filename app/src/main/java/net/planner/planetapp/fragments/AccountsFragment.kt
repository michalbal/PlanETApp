package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.adapters.CalendarAccountAdapter
import net.planner.planetapp.databinding.AccountsFragmentBinding
import net.planner.planetapp.viewmodels.AccountsFragmentViewModel

class AccountsFragment : Fragment() {

    companion object {

        private const val TAG = "AccountsFragment"

    }
    private lateinit var mBinding: AccountsFragmentBinding
    private lateinit var viewModel: AccountsFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = AccountsFragmentBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(AccountsFragmentViewModel::class.java)

        // Init Accounts Recycler View
        val accountsRecycler = mBinding.googleAccountsList
        accountsRecycler.layoutManager = LinearLayoutManager(context)
        accountsRecycler.adapter = CalendarAccountAdapter(listOf())




    }



}