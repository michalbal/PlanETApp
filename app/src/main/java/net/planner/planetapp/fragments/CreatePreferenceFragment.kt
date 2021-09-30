package net.planner.planetapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillValue
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import net.planner.planetapp.R
import net.planner.planetapp.adapters.CalendarAccountAdapter
import net.planner.planetapp.databinding.AccountsFragmentBinding
import net.planner.planetapp.databinding.FragmentCreatePreferenceBinding
import net.planner.planetapp.viewmodels.AccountsFragmentViewModel


class CreatePreferenceFragment : Fragment() {
    companion object {

        private const val TAG = "CreatePreferenceFragment"

    }

    private lateinit var mBinding: FragmentCreatePreferenceBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentCreatePreferenceBinding.inflate(inflater, container, false)
        return mBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Priority choice adapter
        val items = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
        val adapter = ArrayAdapter(requireContext(), R.layout.priority_list_item, items)
        val editPreference = mBinding.editPreferencePriority.editText as? AutoCompleteTextView
        editPreference?.setAdapter(adapter)
        editPreference?.setText("5")

//        viewModel = ViewModelProvider(this).get(AccountsFragmentViewModel::class.java)
//
//        // Init Accounts Recycler View
//        val accountsRecycler = mBinding.googleAccountsList
//        accountsRecycler.layoutManager = LinearLayoutManager(context)
//        accountsRecycler.adapter = CalendarAccountAdapter(listOf())



    }

}