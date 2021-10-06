package net.planner.planetapp.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.IOnPlanCalculatedListener
import net.planner.planetapp.IOnTasksReceivedListener
import net.planner.planetapp.adapters.SubtaskPlanDayRep
import net.planner.planetapp.getDayDate
import net.planner.planetapp.getMillisFromDate
import net.planner.planetapp.planner.PlannerEvent
import net.planner.planetapp.planner.PlannerTask
import net.planner.planetapp.planner.TasksManager
import java.util.*
import kotlin.collections.HashMap

class MainActivityViewModel: ViewModel() {

    companion object {

        private const val TAG = "MainActivityViewModel"

    }

    val showTasksDialog =  MutableLiveData<Collection<PlannerTask>>()
    val showPlanCalculatedDialog =  MutableLiveData<List<SubtaskPlanDayRep>>()

    private val mOnTasksReceivedListener = OnTasksReceivedListener()
    private val mOnPlanCalculatedListener = OnPlanCalculatedListener()

    private var subTasksToSave: List<PlannerEvent> = listOf()



    fun onStart() {
        Log.d(TAG, "onStart: Registering listeners")
        // Register listeners
        TasksManager.getInstance().addTasksReceivedListener(mOnTasksReceivedListener)
        TasksManager.getInstance().addPlanCalculatedListener(mOnPlanCalculatedListener)
    }

    fun onStop() {
        Log.d(TAG, "onStop: UnRegistering listeners")
        // UnRegister listeners
        TasksManager.getInstance().removeTasksReceivedListener(mOnTasksReceivedListener)
        TasksManager.getInstance().removePlanCalculatedListener(mOnPlanCalculatedListener)
    }

    fun startPlanningScheduleForTasks(tasks: ArrayList<PlannerTask>) {
        Log.d(TAG, "startPlanningScheduleForTasks: Received ${tasks.size} tasks")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                TasksManager.getInstance().planSchedule(tasks)
            }
        }
    }

    fun recalculatePlanForTasks(tasks: Collection<PlannerTask>) {
        Log.d(TAG, "recalculatePlanForTasks: Received ${tasks.size} tasks")
        // Check if this task has subTasks. If it has, remove them both from task and
        // google calendar(all events that have passed and reduce the time of those remaining from the estimated duration)
    }

    fun savePlan(subtasks: List<PlannerEvent>) {
        Log.d(TAG, "savePlan: Received ${subtasks.size} subtasks")
        // Inform relevant parties that the plan was approved by the user
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                TasksManager.getInstance().processUserAcceptedSubtasks(subtasks)
            }
        }
    }

    fun savePlan() {
        Log.d(TAG, "savePlan: Saving plan from saved Subtasks")
        // Inform relevant parties that the plan was approved by the user
        if(subTasksToSave.isEmpty()) {
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                TasksManager.getInstance().processUserAcceptedSubtasks(subTasksToSave)
            }
        }
    }

    fun saveSubTasksForLater(subtasks: List<PlannerEvent>) {
        subTasksToSave = subtasks
    }


    private inner class OnTasksReceivedListener: IOnTasksReceivedListener {

        override fun onTasksReceivedFromMoodle(tasks: Collection<PlannerTask>) {
            // Inform mainActivity so it would Show dialog then start calculation for tasks chosen
            Log.d(TAG, "onTasksReceivedFromMoodle: Received ${tasks.size} tasks")
            showTasksDialog.postValue(tasks)
        }

    }

    private inner class OnPlanCalculatedListener: IOnPlanCalculatedListener {
        override fun onPlanCalculated(subtasks: Collection<PlannerEvent>) {
            Log.d(TAG, "OnPlanCalculatedListener: Received ${subtasks.size} subtasks")

            // Create structures of subTasks that the adapter can work with
            var subTasksPerDayMap: HashMap<String, MutableList<PlannerEvent>> = HashMap()
            for(subTask in subtasks) {
                val eventDate = getDayDate(subTask.startTime)
                if(!subTasksPerDayMap.containsKey(eventDate)) {
                    subTasksPerDayMap[eventDate] = mutableListOf<PlannerEvent>()
                }
                subTasksPerDayMap[eventDate]?.add(subTask)
            }

            var subTaskPerDayList = mutableListOf<SubtaskPlanDayRep>()
            for(entry in subTasksPerDayMap.entries) {
                val sortedEvents = entry.value.toSortedSet(Comparator { o1, o2 ->
                    o1.startTime.compareTo(o2.startTime)
                })
                val dayRep = SubtaskPlanDayRep(entry.key, sortedEvents.toList())
                subTaskPerDayList.add(dayRep)
            }

            val sortedDays = subTaskPerDayList.toSortedSet { o1, o2 ->
                getMillisFromDate(o1.dayDate)?.compareTo(getMillisFromDate(o2.dayDate)!!) ?: o1.dayDate.compareTo(o2.dayDate)
            }

            showPlanCalculatedDialog.postValue(sortedDays.toList())
        }
    }

}