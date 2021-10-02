package net.planner.planetapp

import android.app.Activity
import net.planner.planetapp.planner.PlannerCalendar
import java.util.concurrent.TimeUnit

object UserPreferencesManager {

    val preferences = App.context.getSharedPreferences(App.context.packageName, Activity.MODE_PRIVATE)

    // Keys
    private const val USER_NAME = "user_name"
    private const val MOODLE_USER_NAME = "moodle_user_name"
    private const val TOKEN_KEY = "token"
    private const val CALENDAR_ACCOUNTS_KEY = "calendar_accounts"
    private const val MAIN_CALENDAR = "main_calendar_account"
    private const val AVG_TASK_DURATION_MINUTES = "avg_task_duration"
    private const val PREFERRED_SESSION_TIME_MINUTES = "max_session"
    private const val SPACE_BETWEEN_EVENTS_MINUTES = "space_between_events"

    var moodleUserName: String? = null
        set(name) {
            field = name
            preferences.edit().putString(MOODLE_USER_NAME, name).apply()
        }

    var userName: String? = null
        set(name) {
            field = name
            preferences.edit().putString(USER_NAME, name).apply()
        }

    var userMoodleToken: String? = null
        set(token) {
            field = token
            preferences.edit().putString(TOKEN_KEY, token).apply()
        }

    var mainCalendarAccount: String? = null
        set(account) {
            field = account
            preferences.edit().putString(MAIN_CALENDAR, account).apply()
        }

    var calendarAccounts: Set<String>? = null
        set(accounts) {
            field = accounts
            preferences.edit().putStringSet(CALENDAR_ACCOUNTS_KEY, accounts).apply()
        }

    var avgTaskDurationMinutes: Long = TimeUnit.HOURS.toMinutes(5L)
        set(duration) {
            field = duration
            preferences.edit().putLong(AVG_TASK_DURATION_MINUTES, duration).apply()
        }

    var spaceBetweenEventsMinutes: Long = PlannerCalendar.SPACE_IN_MINUTES.toLong()
        set(duration) {
            field = duration
            preferences.edit().putLong(SPACE_BETWEEN_EVENTS_MINUTES, duration).apply()
        }


    var preferredSessionTime: Long = 30L
        set(duration) {
            field = duration
            preferences.edit().putLong(PREFERRED_SESSION_TIME_MINUTES, duration).apply()
        }

    init {
        userMoodleToken = preferences.getString(TOKEN_KEY, null)
        mainCalendarAccount = preferences.getString(MAIN_CALENDAR, null)
        calendarAccounts = preferences.getStringSet(CALENDAR_ACCOUNTS_KEY, null)
        userName = preferences.getString(USER_NAME, "Friend")
        moodleUserName = preferences.getString(MOODLE_USER_NAME, null)
        avgTaskDurationMinutes = preferences.getLong(AVG_TASK_DURATION_MINUTES, TimeUnit.HOURS.toMinutes(5L))
        spaceBetweenEventsMinutes = preferences.getLong(SPACE_BETWEEN_EVENTS_MINUTES, PlannerCalendar.SPACE_IN_MINUTES.toLong())
        preferredSessionTime = preferences.getLong(PREFERRED_SESSION_TIME_MINUTES, 30L)
    }


}