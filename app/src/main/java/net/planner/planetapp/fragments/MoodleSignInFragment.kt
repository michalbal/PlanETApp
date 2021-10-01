package net.planner.planetapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.App
import net.planner.planetapp.R
import net.planner.planetapp.databinding.MoodleSignInFragmentBinding
import net.planner.planetapp.planner.TasksManager

class MoodleSignInFragment : Fragment() {

    companion object {
        private const val TAG = "MoodleSignInFragment"
    }

    private lateinit var mBinding: MoodleSignInFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = MoodleSignInFragmentBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mBinding.loginButton.setOnClickListener { view ->
            Log.d(TAG, "Login was clicked! Getting token from Moodle")
            val userName = mBinding.editMoodleUserName.editText?.text.toString()
            val password = mBinding.editPassword.editText?.text.toString()
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        TasksManager.getInstance().initTasksManager(userName, password)
                        Log.d(TAG, "Found token successfully! Moving to selection screen")
                        activity?.runOnUiThread {
                            val navController = findNavController()
                            navController.navigate(MoodleSignInFragmentDirections.actionMoodleSignInFragmentToMoodleCoursesSelectionFragment())
                        }
                    } catch (e: Exception) {
                        activity?.runOnUiThread {
                            Toast.makeText(App.context, App.context.getText(R.string.login_error), Toast.LENGTH_LONG).show()
                        }
                        Log.e(TAG, "Retrieving from Moodle failed, received error ${e.message}")
                    }
                }
            }
        }

        mBinding.skipButton.setOnClickListener { view ->
            Log.d(TAG, "Skip was clicked! Moving to Initial Settings screen")
            activity?.runOnUiThread {
                val navController = findNavController()
                navController.navigate(MoodleSignInFragmentDirections.actionMoodleSignInFragmentToInitialSettingsFragment())
            }
        }
    }


}