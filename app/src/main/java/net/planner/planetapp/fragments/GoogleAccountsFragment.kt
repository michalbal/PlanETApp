package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.planner.planetapp.R
import net.planner.planetapp.viewmodels.GoogleAccountsFragmentViewModel

class GoogleAccountsFragment : Fragment() {

    companion object {
        fun newInstance() = GoogleAccountsFragment()
    }

    private lateinit var viewModel: GoogleAccountsFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.google_accounts_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(GoogleAccountsFragmentViewModel::class.java)
        // TODO: Use the ViewModel
    }

}