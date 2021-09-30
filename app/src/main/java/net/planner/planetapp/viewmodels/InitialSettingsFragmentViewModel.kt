package net.planner.planetapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.getMillisFromDate
import net.planner.planetapp.planner.TasksManager

class InitialSettingsFragmentViewModel : ViewModel() {
    companion object {

        private const val TAG = "InitialSettingsModel"

    }

    fun startGettingTasksFromMoodle() {
        Log.d(TAG, "startGettingTasksFromMoodle called")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
//                val millisToPlanFrom = System.currentTimeMillis()
                val millisToPlanFrom = getMillisFromDate("20/05/2021") ?: System.currentTimeMillis()
                TasksManager.getInstance().parseMoodleTasks(millisToPlanFrom)
            }
        }
    }
}