package net.planner.planetapp

import android.app.Application
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.planner.planetapp.database.local_database.LocalDBManager
import net.planner.planetapp.networking.GoogleCalenderCommunicator
import net.planner.planetapp.planner.TasksManager
import kotlin.coroutines.coroutineContext

class App: Application()  {
    companion object {
        private val TAG = "App"
        lateinit var context: App
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called. Initializing app context")
        context = this
        // TODO init WorkManager
        UserPreferencesManager.toString()
        // TODO add wait here before continuing to the others?
        GlobalScope.launch {
            LocalDBManager.toString()
            GoogleCalenderCommunicator.initAccountsFromDb(context)
            TasksManager.getInstance()
        }
    }

}