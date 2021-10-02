package net.planner.planetapp.database.local_database

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList
import java.util.HashMap

class Converters {

    private val TAG = "Converters"

    @TypeConverter
    fun stringListToString(list: List<String>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun stringToStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type

        return try {
            Gson().fromJson(value, listType)
        } catch (e: Exception) {
            Log.w(TAG, "stringToStringList: ", e)
            listOf()
        }
    }

    @TypeConverter
    fun stringSetToString(set: Set<String>): String {
        val gson = Gson()
        return gson.toJson(set)
    }

    @TypeConverter
    fun stringToStringSet(value: String): Set<String> {
        val setType = object : TypeToken<Set<String>>() {}.type

        return try {
            Gson().fromJson(value, setType)
        } catch (e: Exception) {
            Log.w(TAG, "stringToStringSet: ", e)
            setOf()
        }
    }

    @TypeConverter
    fun hashMapToString(map: HashMap<String, ArrayList<String>>): String {
        val gson = Gson()
        return gson.toJson(map)
    }

    @TypeConverter
    fun stringToHashMap(value: String): HashMap<String, ArrayList<String>> {
        val setType = object : TypeToken<HashMap<String, ArrayList<String>>>() {}.type

        return try {
            Gson().fromJson(value, setType)
        } catch (e: Exception) {
            Log.w(TAG, "stringToHashMap: ", e)
            HashMap()
        }
    }




}