package net.planner.planetapp.database

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

class CategoryDB (
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("tag")
    val tag: String = "",

    @SerializedName("tasks")
    var tasks: ArrayList<TaskDB?>? = ArrayList(),
)