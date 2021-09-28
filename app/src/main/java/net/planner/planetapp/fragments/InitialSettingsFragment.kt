package net.planner.planetapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import net.planner.planetapp.MainActivity
import net.planner.planetapp.databinding.InitialSettingsFragmentBinding
import net.planner.planetapp.viewmodels.InitialSettingsFragmentViewModel

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
            Log.d(TAG, "Moving on to Home Screen")
            val mainActivity = activity as MainActivity
            mainActivity?.returnBottomNavigation()
            val navController = findNavController()
            navController.navigate(InitialSettingsFragmentDirections.actionInitialSettingsFragmentToHomeFragment())
        }

    }



}