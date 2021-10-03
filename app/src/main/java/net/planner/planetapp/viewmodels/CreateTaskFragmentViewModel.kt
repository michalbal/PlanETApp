package net.planner.planetapp.viewmodels

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.lifecycle.ViewModel
import net.planner.planetapp.UserPreferencesManager
import java.util.concurrent.TimeUnit
import androidx.databinding.library.baseAdapters.BR

class CreateTaskFragmentViewModel : ViewModel() {

    val content = Content()


    class Content() : BaseObservable() {

        @get:Bindable
        var estimatedTimeHours: String = TimeUnit.MINUTES.toHours(UserPreferencesManager.avgTaskDurationMinutes).toString()
            set(taskTime) {
                field = taskTime
                notifyPropertyChanged(BR.estimatedTimeHours)
            }

        @get:Bindable
        var preferredSessionHours: String = TimeUnit.MINUTES.toHours(UserPreferencesManager.preferredSessionTime).toString()
            set(preferredTime) {
                field = preferredTime
                notifyPropertyChanged(BR.preferredSessionHours)
            }

        @get:Bindable
        var minSessionTime: String = UserPreferencesManager.spaceBetweenEventsMinutes.toString()
            set(minSession) {
                field = minSession
                notifyPropertyChanged(BR.minSessionTime)
            }

    }

}