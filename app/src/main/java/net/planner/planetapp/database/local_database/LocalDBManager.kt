package net.planner.planetapp.database.local_database

import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.planner.planetapp.App
import net.planner.planetapp.database.PreferenceDB
import net.planner.planetapp.database.TaskDB
import net.planner.planetapp.planner.PlannerTag
import net.planner.planetapp.planner.PlannerTask
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

object LocalDBManager {

    private const val TAG = "LocalDBManager"
    private const val DATABASE_VERSION = 1
    private const val DATABASE_NAME = "app_db"

    private val mLock = ReentrantLock()

    private val mDb = Room.databaseBuilder(App.context, AppRoomDatabase::class.java, DATABASE_NAME).build()

    val dbLocalTasksData: LiveData<List<TaskLocalDB>>
        get() = mDb.tasksDao().tasks

    val dbLocalPreferencesData: LiveData<List<PreferencesLocalDB>>
        get() = mDb.preferencesDao().preferences

    val dbLocalCoursesData: LiveData<List<CourseLocalDB>>
        get() = mDb.coursesDao().courses

    fun insertOrUpdateTask(task: PlannerTask) {
        var subTaskDates = listOf<String>()

        mDb.tasksDao().getTask(task.moodleId)?.let {
            subTaskDates = it.subtaskDates
        }

        insertOrUpdateTask(task, subTaskDates)
    }


    fun insertOrUpdateTask(task: PlannerTask, subTaskDates: List<String>) {
        val taskToInsert = TaskLocalDB(task.moodleId, task.title, task.courseId, task.description,
            task.location, task.isExclusiveForItsTimeSlot, task.reminder, task.tagName, task.deadline,
            task.priority, task.maxSessionTimeInMinutes, task.maxDivisionsNumber, task.durationInMinutes,
            subTaskDates)

        mLock.withLock {
            mDb.tasksDao().insertOrUpdateTask(taskToInsert)
        }
    }

    fun isTaskInDbAndUnchanged(taskId: String, deadline: Long): Boolean {
        val task: TaskLocalDB?
        mLock.withLock {
            task = mDb.tasksDao().getTask(taskId)
        }
        if (task != null) {
            if (task.deadline == deadline) {
                return true
            }
        }

        return false
    }

    fun getTask(taskId: String): TaskLocalDB? {
        val task: TaskLocalDB?
        mLock.withLock {
            task = mDb.tasksDao().getTask(taskId)
        }
        return task
    }

    fun insertOrUpdatePreference(preference: PreferenceDB) {
        var courseNames = setOf<String>()
        mDb.preferencesDao().getPreference(preference.tagName)?.let {
            courseNames = it.courses
        }

        insertOrUpdatePreference(preference, courseNames)
    }

    fun insertOrUpdatePreference(preference: PreferencesLocalDB) {
        mLock.withLock {
            mDb.preferencesDao().insertOrUpdatePreference(preference)
        }
    }


    fun insertOrUpdatePreference(preference: PreferenceDB, courseNames: Set<String>) {
        val preferenceToInsert = PreferencesLocalDB(preference.tagName, preference.priority,
            preference.preferredTIsettings, preference.forbiddenTIsettings, courseNames)

        mLock.withLock {
            mDb.preferencesDao().insertOrUpdatePreference(preferenceToInsert)
        }
    }

    fun deletePreference(tagName: String) {
        mLock.withLock {
            mDb.preferencesDao().deletePreference(tagName)
        }
    }


    fun insertOrUpdateCourse(courseId: String, courseName: String, tagName: String) {
        val courseToInsert = CourseLocalDB(courseId, courseName, tagName)
        mLock.withLock {
            mDb.coursesDao().insertOrUpdateCourse(courseToInsert)
        }
    }




    @Database(entities = [(TaskLocalDB::class), PreferencesLocalDB::class, CourseLocalDB::class], version = DATABASE_VERSION, exportSchema = true)
    @TypeConverters(Converters::class)
    abstract class AppRoomDatabase : RoomDatabase() {
        abstract fun tasksDao(): TasksDao

        abstract fun preferencesDao(): PreferencesDao

        abstract fun coursesDao(): CoursesDao
    }



}