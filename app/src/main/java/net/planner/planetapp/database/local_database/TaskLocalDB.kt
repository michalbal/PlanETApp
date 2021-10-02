package net.planner.planetapp.database.local_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
class TaskLocalDB (

    @PrimaryKey
    @ColumnInfo(name = "id")
    val taskId: String = "",

    @ColumnInfo(name = "name")
    var name: String = "",

    @ColumnInfo(name = "courseID")
    val courseID: String = "",

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "location")
    val location: String = "",

    @ColumnInfo(name = "exclusiveForItsTimeSlot")
    val exclusiveForItsTimeSlot: Boolean = false,

    @ColumnInfo(name = "reminder")
    val reminder: Int = 0,

    @ColumnInfo(name = "tag")
    val tag: String? = "",

    @ColumnInfo(name = "deadline")
    val deadline: Long = 0,

    @ColumnInfo(name = "priority")
    val priority: Int = 5,

    @ColumnInfo(name = "maxSessionTimeInMinutes")
    val maxSessionTimeInMinutes: Int = 60,

    @ColumnInfo(name = "maxDivisionsNumber")
    val maxDivisionsNumber: Int = 10,

    @ColumnInfo(name = "durationInMinutes")
    val durationInMinutes: Int = 60,

    @ColumnInfo(name = "subtask_dates")
    val subtaskDates: List<String>

)