package net.planner.planetapp.database.local_database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.ArrayList
import java.util.HashMap

@Entity(tableName = "preferences")
class PreferencesLocalDB (

    @PrimaryKey
    @ColumnInfo(name = "tagName")
    val tagName: String = "",

    @ColumnInfo(name ="priority")
    val priority: Int = 5,

    @ColumnInfo(name ="preferredTIsettings")
    val preferredTIsettings: HashMap<String, ArrayList<String>> = HashMap(),

    @ColumnInfo(name ="forbiddenTIsettings")
    val forbiddenTIsettings: HashMap<String, ArrayList<String>> = HashMap(),

    @ColumnInfo(name ="course_names")
    var courses: Set<String>

)