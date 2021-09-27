package net.planner.planetapp.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.planner.planetapp.App
import net.planner.planetapp.planner.TasksManager

class MoodleCoursesSelectionFragmentViewModel : ViewModel() {
        var courses = MutableLiveData<Collection<String>>()

        fun updateCourses() {
            viewModelScope.launch {
                getCourses()
            }
        }

        private suspend fun getCourses() {
//            withContext(Dispatchers.IO) {
//                val moodleCourses = async {
//                    // TODO get moodle courses
////                    TasksManager.getInstance().
//                    listOf()
//                }.await()
//                courses.postValue(moodleCourses)
//            }
        }
}