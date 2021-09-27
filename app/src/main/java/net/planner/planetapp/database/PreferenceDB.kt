package net.planner.planetapp.database

import com.google.gson.annotations.SerializedName
import java.util.*

class PreferenceDB (
    @SerializedName("tagName")
    val tagName: String = "",

    @SerializedName("priority")
    val priority: Int = 5,

    @SerializedName("preferredTIsettings")
    val preferredTIsettings: HashMap<String, ArrayList<String>> = HashMap(),

    @SerializedName("forbiddenTIsettings")
    val forbiddenTIsettings: HashMap<String, ArrayList<String>> = HashMap(),

//    @SerializedName("sunTimes")
//    var sunTimes: ArrayList<String>? = ArrayList(),
//
//    @SerializedName("monTimes")
//    var monTimes: ArrayList<String>? = ArrayList(),
//
//    @SerializedName("tueTimes")
//    var tueTimes: ArrayList<String>? = ArrayList(),
//
//    @SerializedName("wedTimes")
//    var wedTimes: ArrayList<String>? = ArrayList(),
//
//    @SerializedName("thuTimes")
//    var thuTimes: ArrayList<String>? = ArrayList(),
//
//    @SerializedName("friTimes")
//    var friTimes: ArrayList<String>? = ArrayList(),
//
//    @SerializedName("satTimes")
//    var satTimes: ArrayList<String>? = ArrayList()
)