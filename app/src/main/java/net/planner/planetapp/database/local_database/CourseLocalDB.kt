package net.planner.planetapp.database.local_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
class CourseLocalDB (

    @PrimaryKey
    @ColumnInfo(name = "courseId")
    val courseId: String = "",

    @ColumnInfo(name = "courseName")
    var courseName: String = "",

    @ColumnInfo(name = "preferenceTagId")
    var preferenceTagId: String = "generalPreference"

)