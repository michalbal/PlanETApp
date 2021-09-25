package net.planner.planetapp

import android.app.Application
import android.util.Log
import net.planner.planetapp.networking.GoogleCalenderCommunicator

class App: Application()  {
    companion object {
        private val TAG = "App"
        lateinit var context: App
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate() called. Initializing app context")
        context = this
        // TODO init WorkManager and maybe TasksManager
        GoogleCalenderCommunicator.initAccountsFromDb(context)
    }

}