package net.planner.planetapp

import android.app.Activity

object UserPreferencesManager {

    val preferences = App.context.getSharedPreferences(App.context.packageName, Activity.MODE_PRIVATE)

    // Keys
    private const val TOKEN_KEY = "token"
    private const val CALENDAR_ACCOUNTS_KEY = "calendar_accounts"
    private const val MAIN_CALENDAR = "main_calendar_account"
    private const val COURSES_TO_TAGS_KEY = "tag_courses"

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


    init {
        userMoodleToken = preferences.getString(TOKEN_KEY, null)
        mainCalendarAccount = preferences.getString(MAIN_CALENDAR, null)
        calendarAccounts = preferences.getStringSet(CALENDAR_ACCOUNTS_KEY, null)
    }


}