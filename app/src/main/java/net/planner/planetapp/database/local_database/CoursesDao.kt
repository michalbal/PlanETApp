package net.planner.planetapp.database.local_database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CoursesDao {

    @get:Query("SELECT * FROM courses")
    val courses: LiveData<List<CourseLocalDB>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateCourse(course: CourseLocalDB) : Long

    @Delete
    fun deleteCourse(course: CourseLocalDB) : Int

    @Query("DELETE FROM courses WHERE courseId = :id")
    fun deleteCourse(id: String) : Int

    @Query("SELECT * FROM courses WHERE courseId = :id")
    fun getCourse(id: String): CourseLocalDB

    @Query("SELECT * FROM courses WHERE courseName = :name")
    fun getCourseFromName(name: String): CourseLocalDB

    @Query("DELETE FROM courses")
    fun deleteAllCourses()

}