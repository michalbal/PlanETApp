package net.planner.planetapp.database

import com.google.gson.annotations.SerializedName
import java.util.*

class TaskDB(
    @SerializedName("id")
    val taskId: String = "",

    @SerializedName("name")
    var name: String = "",

    @SerializedName("description")  // TODO do we need it?
    val description: String = "",

    @SerializedName("location")  // TODO do we need it?
    val location: String = "",

    @SerializedName("exclusiveForItsTimeSlot")
    val exclusiveForItsTimeSlot: Boolean = false,

    @SerializedName("reminder")
    val reminder: Int = 0,

    @SerializedName("tag")
    val tag: String? = "",

    @SerializedName("deadline")
    val deadline: Long = 0,

    @SerializedName("priority")
    val priority: Int = 5,

    @SerializedName("maxSessionTimeInMinutes")
    val maxSessionTimeInMinutes: Int = 60,

    @SerializedName("maxDivisionsNumber")
    val maxDivisionsNumber: Int = 10,

    @SerializedName("durationInMinutes")
    val durationInMinutes: Int = 60,

    @SerializedName("subtasks")
    var subtasks: ArrayList<SubtaskDB?>? = ArrayList()
)