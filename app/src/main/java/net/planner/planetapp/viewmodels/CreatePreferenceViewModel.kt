package net.planner.planetapp.viewmodels

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.ViewModel
import net.planner.planetapp.database.local_database.PreferencesLocalDB
import java.util.ArrayList
import java.util.HashMap

class CreatePreferenceViewModel: ViewModel() {

    lateinit var content: Content

    class Content(preference: PreferencesLocalDB?) : BaseObservable() {

        @get:Bindable
        var preferenceScreenTitle: String = if(preference === null) { "Create a new Preference" } else {  "Edit Preference" }
            set(title) {
                field = title
                notifyPropertyChanged(BR.preferenceScreenTitle)
            }

        @get:Bindable
        var isEditingPreference: Boolean = preference == null
            set(editing) {
                field = editing
                notifyPropertyChanged(BR.editingPreference)
            }

        @get:Bindable
        var preferenceName: String = preference?.tagName ?: ""
            set(name) {
                field = name
                notifyPropertyChanged(BR.preferenceName)
            }

        @get:Bindable
        var preferencePriority: String = (preference?.priority ?: 5).toString()
            set(priority) {
                field = priority
                notifyPropertyChanged(BR.preferencePriority)
            }


        @get:Bindable
        var forbiddenTimes: HashMap<String, ArrayList<String>> = preference?.forbiddenTIsettings ?: HashMap()

        @get:Bindable
        var preferredTimes: HashMap<String, ArrayList<String>> = preference?.preferredTIsettings ?: HashMap()

        @get:Bindable
        var courses: Set<String> = preference?.courses ?: setOf()

    }


}