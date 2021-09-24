package net.planner.planetapp.database

import com.google.gson.annotations.SerializedName

class SubtaskDB(
    @SerializedName("google_calendar_id")
    val eventIdGC: String = "",

    @SerializedName("startTime")
    var startTime: Long = 0,

    @SerializedName("endTime")
    val endTime: Long = 0,

    @SerializedName("isAllDay")
    val isAllDay: Boolean = false
)