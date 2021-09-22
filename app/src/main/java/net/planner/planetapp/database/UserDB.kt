package net.planner.planetapp.database

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class UserDB (
    @SerializedName("username")
    val username: String = "",

    @SerializedName("token")
    val token: String = "",

    @SerializedName("category")
    var categoryDB: ArrayList<CategoryDB?>? = ArrayList(),

    @SerializedName("unwanted_tasks")
    var unwantedTasks: ArrayList<UnwantedTasksDB?>? = ArrayList(),

    @SerializedName("unwanted_courses")
    var unwantedCourses: ArrayList<UnwantedCoursesDB?>? = ArrayList(),
)