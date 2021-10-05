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
import net.planner.planetapp.fragments.MoodleSignInFragment
import net.planner.planetapp.getMillisFromDate
import net.planner.planetapp.planner.TasksManager

class MoodleCoursesSelectionFragmentViewModel : ViewModel() {

    companion object {

        private const val TAG = "MoodleSelectionModel"

    }

    var courses = MutableLiveData<Collection<String>>()

    private var courseNamesToIds = HashMap<String, String>()

    fun updateCourses() {
        viewModelScope.launch {
            getCourses()
        }
    }

    private suspend fun getCourses() {
        withContext(Dispatchers.IO) {
            val moodleCourseIdsToNames = async {
                Log.d(TAG, "getCourses: Calling to get course id's from moodle")
                TasksManager.getInstance().parseMoodleCourses()
            }.await()

            Log.d(TAG, "getCourses: Finished getting course id's from moodle ")
            val courseNames = mutableSetOf<String>()
            moodleCourseIdsToNames?.let {
                for(entry in it.entries)  {
                    val name = entry.value
                    val id = entry.key
                    courseNames.add(name)
                    courseNamesToIds.put(name, id)
                }
            }
            Log.d(TAG, "getCourses: Updating fragment with the course names")

            withContext(Dispatchers.Main) {
                courses.postValue(courseNames)
            }
        }
    }

    fun saveChosenCourses(courseNames: Collection<String>) {
        viewModelScope.launch {
            saveCourses(courseNames)
        }
    }

    private suspend fun saveCourses(courseNames: Collection<String>) {
        withContext(Dispatchers.IO) {
            // Make them into a data structure TasksManager knows
            val moodleCourseIdsToNames = HashMap<String, String>()
            for (name in courseNames)  {
                courseNamesToIds[name]?.let {
                    moodleCourseIdsToNames.put(it, name)
                }
            }
            Log.d(TAG, "saveCourses: Calling to save chosen courses")
            TasksManager.getInstance().saveChosenMoodleCourses(moodleCourseIdsToNames)
        }
    }

    fun startGettingTasksFromMoodle() {
        Log.d(TAG, "startGettingTasksFromMoodle called")
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val millisToPlanFrom = System.currentTimeMillis()
//                val millisToPlanFrom = getMillisFromDate("20/05/2021") ?: System.currentTimeMillis()
                TasksManager.getInstance().parseMoodleTasks(millisToPlanFrom)
            }
        }
    }
}