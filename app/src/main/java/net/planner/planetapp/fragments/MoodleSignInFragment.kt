package net.planner.planetapp.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
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
            val userName = mBinding.editMoodleUserName.text.toString()
            val password = mBinding.editPassword.text.toString()
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        TasksManager.getInstance().initTasksManager(userName, password)

                    } catch (e: Exception) {
                        Toast.makeText(App.context, App.context.getText(R.string.login_error), Toast.LENGTH_LONG)
                        Log.e(TAG, "Retrieving from Moodle failed, received error ${e.message}")
                    }
                }
            }
        }
    }


}