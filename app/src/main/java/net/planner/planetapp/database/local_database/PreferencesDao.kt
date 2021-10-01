package net.planner.planetapp.database.local_database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PreferencesDao {

    @get:Query("SELECT * FROM preferences")
    val preferences: LiveData<List<PreferencesLocalDB>>

    @get:Query("SELECT * FROM preferences")
    val preferencesSlow: List<PreferencesLocalDB>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdatePreference(preference: PreferencesLocalDB) : Long

    @Delete
    fun deletePreference(preference: PreferencesLocalDB) : Int

    @Query("DELETE FROM preferences WHERE tagName = :name")
    fun deletePreference(name: String) : Int

    @Query("SELECT * FROM preferences WHERE tagName = :name")
    fun getPreference(name: String): PreferencesLocalDB?

    @Query("DELETE FROM preferences")
    fun deleteAllPreferences()

}