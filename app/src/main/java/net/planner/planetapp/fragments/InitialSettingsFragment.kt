package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.planner.planetapp.R
import net.planner.planetapp.viewmodels.InitialSettingsFragmentViewModel

class InitialSettingsFragment : Fragment() {

    companion object {
        fun newInstance() = InitialSettingsFragment()
    }

    private lateinit var viewModel: InitialSettingsFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.initial_settings_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InitialSettingsFragmentViewModel::class.java)
        // TODO: Use the ViewModel
    }

}