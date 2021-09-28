package net.planner.planetapp

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.planner.planetapp.database.local_database.LocalDBManager
import net.planner.planetapp.networking.GoogleCalenderCommunicator
import net.planner.planetapp.planner.TasksManager
import net.planner.planetapp.workers.CheckForNewTasksWorker
import java.util.concurrent.TimeUnit
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
        val uniqueName = "PeriodicMoodleUpdate" // TODO save in utils or in shared preferences

        val periodicWorkReq = PeriodicWorkRequestBuilder<CheckForNewTasksWorker>(24, TimeUnit.HOURS)
            .addTag(uniqueName)
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(uniqueName, ExistingPeriodicWorkPolicy.KEEP, periodicWorkReq)

        UserPreferencesManager.toString()
        // TODO add wait here before continuing to the others?
        GlobalScope.launch {
            LocalDBManager.toString()
            GoogleCalenderCommunicator.initAccountsFromDb(context)
            TasksManager.getInstance()
        }
    }

}