package net.planner.planetapp

import net.planner.planetapp.planner.PlannerEvent
import net.planner.planetapp.planner.PlannerTask

interface IOnTasksReceivedListener {

    fun onTasksReceivedFromMoodle(tasks: Collection<PlannerTask>)

}

interface IOnPlanCalculatedListener {

     fun onPlanCalculated(subtasks: Collection<PlannerEvent>)

}
