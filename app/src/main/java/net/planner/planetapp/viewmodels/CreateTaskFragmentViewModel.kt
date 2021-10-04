package net.planner.planetapp.viewmodels

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.ViewModel
import net.planner.planetapp.UserPreferencesManager
import java.util.concurrent.TimeUnit
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.database.local_database.TaskLocalDB
import net.planner.planetapp.planner.PlannerObject
import net.planner.planetapp.planner.PlannerTask
import net.planner.planetapp.planner.TasksManager

class CreateTaskFragmentViewModel : ViewModel() {

    lateinit var content: Content

    fun saveTask(taskUpdated: TaskLocalDB) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val taskPlanner = PlannerTask(taskUpdated)
                TasksManager.getInstance().saveTask(taskPlanner)
            }
        }
    }


    class Content(task: TaskLocalDB?) : BaseObservable() {

        @get:Bindable
        var taskScreenTitle: String = if(task === null) { "Create Task" } else {  "Edit Task" }
            set(title) {
                field = title
                notifyPropertyChanged(BR.taskScreenTitle)
            }

        @get:Bindable
        var isEditingTask: Boolean = task == null
            set(editing) {
                field = editing
                notifyPropertyChanged(BR.editingTask)
            }

        @get:Bindable
        var estimatedTimeHours: String = TimeUnit.MINUTES.toHours(task?.durationInMinutes?.toLong() ?: UserPreferencesManager.avgTaskDurationMinutes).toString()
            set(taskTime) {
                field = taskTime
                notifyPropertyChanged(BR.estimatedTimeHours)
            }

        @get:Bindable
        var preferredSessionHours: String = TimeUnit.MINUTES.toHours(task?.maxSessionTimeInMinutes?.toLong() ?:UserPreferencesManager.preferredSessionTime).toString()
            set(preferredTime) {
                field = preferredTime
                notifyPropertyChanged(BR.preferredSessionHours)
            }

        @get:Bindable
        var maxNumSessions: String = (task?.maxDivisionsNumber ?: 10).toString()
            set(numSessions) {
                field = numSessions
                notifyPropertyChanged(BR.maxNumSessions)
            }

        @get:Bindable
        var taskPriority: String = (task?.priority ?: 5).toString()
            set(priority) {
                field = priority
                notifyPropertyChanged(BR.taskPriority)
            }

        @get:Bindable
        var taskPreferenceName: String = task?.tag ?: PlannerObject.GENERAL_TAG
            set(preference) {
                field = preference
                notifyPropertyChanged(BR.taskPreferenceName)
            }

    }

}