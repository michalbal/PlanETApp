package net.planner.planetapp.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.App
import net.planner.planetapp.getDayDate
import net.planner.planetapp.getMillisFromDateAndTime
import net.planner.planetapp.networking.GoogleCalenderCommunicator
import net.planner.planetapp.planner.PlannerEvent

class DayFragmentViewModel : ViewModel() {

    companion object {

        private const val TAG = "DayFragmentModel"

    }

    var eventsToDisplay = MutableLiveData<MutableCollection<PlannerEvent>>()

    private var dayShown: String = getDayDate(System.currentTimeMillis())


    fun updateEventsForDay(date: String) {
        dayShown = date
        viewModelScope.launch {
            getEventsForDay()
        }
    }

    private suspend fun getEventsForDay() {
        val events = withContext(Dispatchers.IO) {
            async {
                Log.d(TAG, "getEventsForDay: Getting events from Google Calendar")
                val startDayTime = getMillisFromDateAndTime(dayShown + " 00:00")
                val endDayTime = getMillisFromDateAndTime(dayShown + " 23:59")
                GoogleCalenderCommunicator.getUserEvents(App.context, startDayTime, endDayTime)
            }.await()
        }
        Log.d(TAG, "getEventsForDay: Finished waiting")
        events.let {
            withContext(Dispatchers.Main) {
                Log.d(TAG, "Updating Day fragment with the events")
                eventsToDisplay.postValue(it)
            }
        }
        }

    fun onResume() {
        updateEventsForDay(dayShown)
    }


}