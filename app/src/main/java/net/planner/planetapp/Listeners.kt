package net.planner.planetapp

import androidx.annotation.WorkerThread
import net.planner.planetapp.database.PreferenceDB
import net.planner.planetapp.database.TaskDB
import net.planner.planetapp.planner.PlannerEvent
import net.planner.planetapp.planner.PlannerTag
import net.planner.planetapp.planner.PlannerTask

@WorkerThread
interface IOnTasksReceivedListener {

    fun onTasksReceivedFromMoodle(tasks: Collection<PlannerTask>)

}

@WorkerThread
interface IOnPlanCalculatedListener {

     fun onPlanCalculated(subtasks: Collection<PlannerEvent>)

}

@WorkerThread
interface IOnTaskReceivedFromDB {
    fun onTaskReceived(task: PlannerTask)
}

@WorkerThread
interface IOnPreferenceReceivedFromDB {
    fun onPreferenceReceived(preference: PreferenceDB)
}
