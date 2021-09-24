package net.planner.planetapp.database

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class UserDB (
    @SerializedName("username")
    val username: String = "",

    @SerializedName("unwantedTasks")
    var unwantedTasks: ArrayList<String>? = ArrayList(),

    @SerializedName("unwantedCourses")
    var unwantedCourses: ArrayList<String>? = ArrayList()
)