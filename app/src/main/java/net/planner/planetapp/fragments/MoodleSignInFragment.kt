package net.planner.planetapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.App
import net.planner.planetapp.R
import net.planner.planetapp.UserPreferencesManager
import net.planner.planetapp.databinding.MoodleSignInFragmentBinding
import net.planner.planetapp.planner.TasksManager
import net.planner.planetapp.viewmodels.HomeFragmentViewModel
import net.planner.planetapp.viewmodels.MoodleSignInFragmentViewModel

class MoodleSignInFragment : Fragment() {

    companion object {
        private const val TAG = "MoodleSignInFragment"
    }

    private lateinit var mBinding: MoodleSignInFragmentBinding
    private lateinit var viewModel: MoodleSignInFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = MoodleSignInFragmentBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(MoodleSignInFragmentViewModel::class.java)

        mBinding.loginButton.setOnClickListener { view ->
            Log.d(TAG, "Login was clicked! Getting token from Moodle")
            val userName = mBinding.editMoodleUserName.editText?.text.toString().trim()
            val password = mBinding.editPassword.editText?.text.toString().trim()

            viewModel.getToken(userName, password)

//            lifecycleScope.launch {
//                withContext(Dispatchers.IO) {
//                    try {
//                        TasksManager.getInstance().initTasksManager(userName, password)
//                        Log.d(TAG, "Found token successfully! Moving to selection screen")
//                        activity?.runOnUiThread {
//                            val navController = findNavController()
//                            navController.navigate(MoodleSignInFragmentDirections.actionMoodleSignInFragmentToMoodleCoursesSelectionFragment())
//                        }
//                    } catch (e: Exception) {
//                        activity?.runOnUiThread {
//                            Toast.makeText(App.context, App.context.getText(R.string.login_error), Toast.LENGTH_LONG).show()
//                        }
//                        Log.e(TAG, "Retrieving from Moodle failed, received error ${e.message}")
//                    }
//                }
//            }
        }

        viewModel.canContinue.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it) {
                    activity?.runOnUiThread {
                        val navController = findNavController()
                        navController.navigate(MoodleSignInFragmentDirections.actionMoodleSignInFragmentToMoodleCoursesSelectionFragment())
                    }

                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(App.context, App.context.getText(R.string.login_error), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        )

        mBinding.skipButton.setOnClickListener { view ->
            Log.d(TAG, "Skip was clicked! Moving to Initial Settings screen")
            activity?.runOnUiThread {
                TasksManager.getInstance().initTasksManager(UserPreferencesManager.userName)
                val navController = findNavController()
                navController.navigate(MoodleSignInFragmentDirections.actionMoodleSignInFragmentToInitialSettingsFragment())
            }
        }
    }


}