package net.planner.planetapp.database.local_database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TasksDao {

    @get:Query("SELECT * FROM tasks")
    val tasks: LiveData<List<TaskLocalDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateTask(task: TaskLocalDB) : Long

    @Delete
    fun deleteTask(task: TaskLocalDB) : Int

    @Query("DELETE FROM tasks WHERE id = :id")
    fun deleteTask(id: String) : Int

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTask(id: String): TaskLocalDB?

    @Query("DELETE FROM tasks")
    fun deleteAllTasks()


}