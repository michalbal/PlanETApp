package net.planner.planetapp.database

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

class CourseDB (
    @SerializedName("courseId")
    val courseId: String = "",

    @SerializedName("courseName")
    var courseName: String = "",

    @SerializedName("preferenceTagId")
    var preferenceTagId: String = "generalPreference"
)