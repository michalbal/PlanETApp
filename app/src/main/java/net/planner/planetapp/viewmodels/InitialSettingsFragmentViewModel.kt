package net.planner.planetapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.R
import net.planner.planetapp.fragments.InitialSettingsFragment
import net.planner.planetapp.getMillisFromDate
import net.planner.planetapp.planner.TasksManager
import java.util.concurrent.TimeUnit

class InitialSettingsFragmentViewModel : ViewModel() {
    companion object {

        private const val TAG = "InitialSettingsModel"

    }

    fun isAvgTaskInputValid(input: String): Boolean {
        try {
            val avgTaskHours = input.toDouble().toLong()
            val avgTaskMinutes = TimeUnit.HOURS.toMinutes(avgTaskHours)
            if (avgTaskMinutes < 30 || avgTaskMinutes > 6000) {
                Log.d(TAG, "testInputIntegrity: Avg Hours with wrong value! value is $avgTaskHours")
                return false
            } else {
                return true
            }
        } catch (e: java.lang.Exception) {
            return false
        }
    }


    fun isPreferredTimeInputValid(input: String, avgTaskHours: Double): Boolean {
        try {
            val preferredSessionTimeHours = input.toDouble().toLong()
            val preferredSessionTimeMinutes = TimeUnit.HOURS.toMinutes(preferredSessionTimeHours)
            val avgTaskMinutes = TimeUnit.HOURS.toMinutes(avgTaskHours.toLong())
            if (preferredSessionTimeMinutes < 30 || preferredSessionTimeMinutes > avgTaskMinutes) {
                Log.d(TAG, "isPreferredTimeInputValid: preferredSessionTimeHours with wrong value! value is $preferredSessionTimeHours")
                return false
            }
            return true
        } catch (e: java.lang.Exception) {
            return false
        }
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