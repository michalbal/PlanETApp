package net.planner.planetapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.UserPreferencesManager
import net.planner.planetapp.database.local_database.LocalDBManager
import net.planner.planetapp.planner.PlannerObject
import net.planner.planetapp.planner.TasksManager

class AccountsFragmentViewModel : ViewModel() {

    fun moodleLogout() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // Logout for TasksManager
                TasksManager.getInstance().logoutOfMoodleUser()

                val generalPreference = LocalDBManager.getPreference(PlannerObject.GENERAL_TAG)
                generalPreference?.courses  = setOf()

                // Remove token from UserPreferences
                UserPreferencesManager.userMoodleToken = null
                UserPreferencesManager.moodleUserName = null

                // Clear Local DB
                LocalDBManager.clearTasks()
                LocalDBManager.clearCourses()
                LocalDBManager.clearPreferences()

                // Reinsert GENERAL PREFERENCE
                generalPreference?.let {
                    LocalDBManager.insertOrUpdatePreference(it)
                }
            }
        }

    }
}