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
import net.planner.planetapp.getTodayTimeMillis
import net.planner.planetapp.networking.GoogleCalenderCommunicator
import net.planner.planetapp.planner.PlannerEvent

class HomeFragmentViewModel : ViewModel() {

    companion object {

        private const val TAG = "HomeFragmentModel"

    }


    var eventsToDisplay = MutableLiveData<MutableCollection<PlannerEvent>>()

    fun getEvents() {
        viewModelScope.launch {
            val events = withContext(Dispatchers.IO) {
                async {
                    Log.d(TAG, "getEvents: Getting events from Google Calendar")
//                    val startDayTime = System.currentTimeMillis()
                    val startDayTime = getTodayTimeMillis()
                    val endDayTime = getMillisFromDateAndTime(getDayDate(startDayTime) + " 23:59")
                    GoogleCalenderCommunicator.getUserEvents(App.context, startDayTime, endDayTime)
                }.await()
            }
            Log.d(TAG, "getEvents: Finished waiting")
            events.let {
                withContext(Dispatchers.Main) {
                    eventsToDisplay.postValue(it)
                }
            }
        }
    }




}