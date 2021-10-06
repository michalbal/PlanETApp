package net.planner.planetapp.networking

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.planner.planetapp.App
import net.planner.planetapp.UserPreferencesManager
import net.planner.planetapp.getDayDate
import net.planner.planetapp.getTodayTimeMillis
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import net.planner.planetapp.planner.PlannerEvent
import java.lang.Exception

object GoogleCalenderCommunicator {

    private val TAG = "GoogleCalenderCommunicator"
    private val ONE_MONTH_MILLIS = TimeUnit.DAYS.toMillis(30)

    // TODO On init, retrieve desired account names from inner DB if exists

    private var calenderIds = mutableSetOf<Long>()
    private var mainCalendarID =
        1L // Calendar to insert new events into by default. Selected as a calendar with the ending gmail.com or  default 1

    private val accountToIdMap = HashMap<String, Long>()

    suspend fun initAccountsFromDb(caller: Context) {
        withContext(Dispatchers.IO) {
            if (!haveCalendarReadPermissions(caller)) {
                Log.d(TAG, "Needed permissions - returning")
                return@withContext
            }

            val accountsSaved = UserPreferencesManager.calendarAccounts

            Log.d(TAG, "initAccountsFromDb: Retrieveing accounts")
            findAccountCalendars(caller)

            accountsSaved?.let {
                Log.d(TAG, "initAccountsFromDb: Found accounts saved in preferences!")
                setCalendarAccounts(it)
            }

            val mainAccount = UserPreferencesManager.mainCalendarAccount
            mainAccount?.let {
                Log.d(TAG, "initAccountsFromDb: Found main account saved in preferences!")
                setMainCalendar(it)
            }

        }

    }



    // Projection array. Creating indices for this array instead of doing
    // dynamic lookups improves performance.
    private val CALENDAR_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Calendars._ID,
        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
        CalendarContract.Calendars.OWNER_ACCOUNT,
        CalendarContract.Calendars.IS_PRIMARY
    )

    private val EVENT_INSTANCE_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Instances.EVENT_ID,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END
    )


    private val EVENT_PROJECTION: Array<String> = arrayOf(
        CalendarContract.Events._ID,
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DESCRIPTION,
        CalendarContract.Events.ALL_DAY,
        CalendarContract.Events.AVAILABILITY,
        CalendarContract.Events.EVENT_LOCATION,
        CalendarContract.Events.DISPLAY_COLOR,
        CalendarContract.Events.CUSTOM_APP_PACKAGE, // To recognise events added by this app
        CalendarContract.Events.CUSTOM_APP_URI // Saves the id of the Task relating to this event
    )

    const val PERMISSION_REQUEST_CODE = 99

    // The indices for Calender projection array
    private const val PROJECTION_ID_INDEX: Int = 0
    private const val PROJECTION_DISPLAY_NAME_INDEX: Int = 1
    private const val PROJECTION_OWNER_ACCOUNT_INDEX: Int = 2
    private const val PROJECTION_IS_MAIN_ACCOUNT_INDEX: Int = 3

    // The indices for Event Instances projection array
    private const val PROJECTION_EVENT_INSTANCE_ID_INDEX: Int = 0
    private const val PROJECTION_EVENT_INSTANCE_BEGIN_INDEX: Int = 1
    private const val PROJECTION_EVENT_INSTANCE_END_INDEX: Int = 2

    // The indices for Event projection array
    private const val PROJECTION_EVENT_ID_INDEX: Int = 0
    private const val PROJECTION_EVENT_TITLE_INDEX: Int = 1
    private const val PROJECTION_DESCRIPTION_INDEX: Int = 2
    private const val PROJECTION_ALL_DAY_INDEX: Int = 3
    private const val PROJECTION_AVAILABILITY_INDEX: Int = 4
    private const val PROJECTION_EVENT_LOCATION_INDEX: Int = 5
    private const val PROJECTION_DISPLAY_COLOR_INDEX: Int = 6
    private const val PROJECTION_APP_CREATED_NAME_INDEX: Int = 7
    private const val PROJECTION_TASK_INFORMATION_INDEX: Int = 8

    /**
     * Get all the ids and names of calendars associated with accounts logged in to the device
     */
    fun findAccountCalendars(activity: Context): Collection<String> {

        val contentResolver = activity.contentResolver
        // Run query to get all calendars in device
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val cur: Cursor? = contentResolver.query(uri, CALENDAR_PROJECTION, null, null, null)

        // Get calender information
        while (cur?.moveToNext() == true) {
            val calenderID: Long = cur.getLong(PROJECTION_ID_INDEX)
            val displayName: String = cur.getString(PROJECTION_DISPLAY_NAME_INDEX)
            val ownerName: String = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX)
            val primary: Int = cur.getInt(PROJECTION_IS_MAIN_ACCOUNT_INDEX)

            accountToIdMap[displayName] = calenderID

            if (displayName.contains("gmail") && primary == 1) {
                Log.d(TAG, "findAccountCalendars: Primary and Gmail!")
                mainCalendarID = calenderID
            }

            calenderIds.add(calenderID)
            Log.d(
                TAG,
                "findAccountCalendars: Found calender with name $displayName and id $calenderID of owner $ownerName "
            )
        }
        cur?.close()

        return accountToIdMap.keys
    }

    /**
     * Set the calendar new events are to be added to, according to google calendar account id
     */
    fun setMainCalendar(calendarId: Long) {
        mainCalendarID = calendarId
    }

    /**
     * Set the calendar new events are to be added to, according to account name
     */
    fun setMainCalendar(accountName: String) {
        if(accountToIdMap.containsKey(accountName)) {
            Log.d(TAG, "setMainCalendar: Found the id for account name $accountName!")
            UserPreferencesManager.mainCalendarAccount = accountName
            mainCalendarID = accountToIdMap[accountName] ?: mainCalendarID
            return
        }

        Log.d(TAG, "setMainCalendar: Could not find the id for account name $accountName")
    }

    fun setCalendarAccounts(accounts: Collection<String>) {
        UserPreferencesManager.calendarAccounts = accounts.toSet()
        var accountIds = mutableSetOf<Long>()
        for (account in accounts) {
            accountIds.add(accountToIdMap[account] ?: 1L)
        }
        calenderIds = accountIds
    }


    fun insertEvent(
        activity: Context, insertedEvent: PlannerEvent): Long {
        if (haveCalendarWritePermissions(activity)) {
            return insertEvent(activity, insertedEvent, mainCalendarID, null)
        }
        return System.currentTimeMillis()
    }

    /**
     * Add event to the google calendar sent.
     * If not specified, the event will be added to the main calendar found by the library, or specified with set.
     */
    fun insertEvent(
        activity: Context, insertedEvent: PlannerEvent,
        calenderId: Long = mainCalendarID, timezone: String? = null
    ): Long {
        val contentResolver = activity.contentResolver
        val startMillis = insertedEvent.startTime
        val endMillis = insertedEvent.endTime
        val eventTitle = insertedEvent.title
        val eventDescription = insertedEvent.description

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, eventTitle)
            put(CalendarContract.Events.DESCRIPTION, eventDescription ?: "")
            put(CalendarContract.Events.CALENDAR_ID, calenderId)
            put(
                CalendarContract.Events.EVENT_TIMEZONE,
                timezone ?: TimeZone.getDefault().displayName
            )
            // Adding Task information
            put(CalendarContract.Events.CUSTOM_APP_PACKAGE, App.context.packageName)
            put(CalendarContract.Events.CUSTOM_APP_URI, insertedEvent.parentTaskId)
        }
        val uri: Uri? = contentResolver?.insert(CalendarContract.Events.CONTENT_URI, values)

        // get the event ID that is the last element in the Uri
        val eventID: Long = uri?.let {
            it.lastPathSegment?.toLong()
        } ?: 0
        return eventID
    }

    /**
     * Get all events in the calendar, from start time to end time. Times are given as milliseconds since 1970.
     */
    private fun getCalendarEvents(
        contentResolver: ContentResolver,
        calenderId: Long,
        startMillis: Long,
        endMillis: Long
    ): MutableCollection<PlannerEvent> {
        // Getting events process:
        // 1. Get the event information from the Events table.
        // 2. For each event, check if it's an all day event.
        // 3. For each event, add all the instances from the Instances table.
        // 4. For each instance, convert time to current timezone if needed.

        val events = mutableListOf<PlannerEvent>()

        // Run query to get all event instances in the calendar
        val instancesSelection: String = "${CalendarContract.Instances.CALENDAR_ID} = ?"

        val builder: Uri.Builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        val selectionArgs: Array<String> = arrayOf(calenderId.toString())

        val instancesCur: Cursor? = contentResolver.query(
            builder.build(),
            EVENT_INSTANCE_PROJECTION,
            instancesSelection,
            selectionArgs,
            null
        )

        while (instancesCur?.moveToNext() == true) {
            // Get the instance values
            val eventID: Long = instancesCur.getLong(PROJECTION_EVENT_INSTANCE_ID_INDEX)
            val eventStart: String = instancesCur.getString(PROJECTION_EVENT_INSTANCE_BEGIN_INDEX)
            val eventEnd: String = instancesCur.getString(PROJECTION_EVENT_INSTANCE_END_INDEX)

            // Note - The Calendar object is created with default Locale and Timezone. It expects time in milliseconds in UTC from the epoch and converts it to the default timezone
            val startDate = Calendar.getInstance().apply {
                timeInMillis = eventStart.toLong()
            }

            val endDate = Calendar.getInstance().apply {
                timeInMillis = eventEnd.toLong()
            }
            Log.d(
                TAG,
                "getCalendarEvents: found event with id $eventID, starts ${
                    getDayDate(startDate.timeInMillis)
                } ends ${getDayDate(endDate.timeInMillis)} from calendar $calenderId "
            )
            events.addAll(getInstancesOfEvent(contentResolver, calenderId, startDate.timeInMillis, endDate.timeInMillis, eventID))
        }
        instancesCur?.close()
        return events
    }

    /**
     * Get all instances of event with the sent id in the specified calendar.
     */
    private fun getInstancesOfEvent(contentResolver: ContentResolver,
                                    calenderId: Long,
                                    startMillis: Long,
                                    endMillis: Long,
                                    eventID: Long
    ): MutableCollection<PlannerEvent> {
        val events = mutableListOf<PlannerEvent>()

        // Get the details of this event from the Events table
        val eventsUri: Uri = CalendarContract.Events.CONTENT_URI
        val eventsSelection: String = "${CalendarContract.Events._ID} = ?"
        val eventSelectionArgs: Array<String> = arrayOf(eventID.toString())
        val eventsCur: Cursor? = contentResolver.query(
            eventsUri,
            EVENT_PROJECTION,
            eventsSelection,
            eventSelectionArgs,
            null
        )

        while (eventsCur?.moveToNext() == true) {
            val id: Long = eventsCur.getLong(PROJECTION_EVENT_ID_INDEX)
            val title: String = eventsCur.getString(PROJECTION_EVENT_TITLE_INDEX)
            val description: String = eventsCur.getString(PROJECTION_DESCRIPTION_INDEX)
            val allDay: Int = eventsCur.getInt(PROJECTION_ALL_DAY_INDEX)
            val availability: Int = eventsCur.getInt(PROJECTION_AVAILABILITY_INDEX)
            val location: String = eventsCur.getString(PROJECTION_EVENT_LOCATION_INDEX)
            val color: Int = eventsCur.getInt(PROJECTION_DISPLAY_COLOR_INDEX)

            var taskId: String = ""
            try {
                val appPackage: String = eventsCur.getString(PROJECTION_APP_CREATED_NAME_INDEX)
                if (appPackage.equals(App.context.packageName)) {
                    Log.d(TAG, "This event was created by our app! must have task information")
                    taskId = eventsCur.getString(PROJECTION_TASK_INFORMATION_INDEX)
                }

            } catch (e: Exception) {
                Log.d(TAG, "No task found for event $title")
            }


            Log.d(
                TAG,
                "getCalendarEvents: found event with id $id, titled ${title} with description: ${description} is all day? $allDay can be scheduled over? $availability, in location $location  with color: $color from calendar: $calenderId "
            )

            if (allDay != 1) {
                // We only want to know about not all day events
                Log.d(TAG, "Event is not all day, adding")
                val event = PlannerEvent(taskId, title, startMillis, endMillis, allDay == 1, id,
                    description, CalendarContract.Events.AVAILABILITY_BUSY != 0, color.toString(), location)

                events.add(event)
            }
        }
        eventsCur?.close()
        return events
    }


    /**
     * Get all the events of the user logged in to Google Calendar app, from all accounts.
     * User must specify start and end time in milliseconds since 1970.
     * If start and end times were not specified, current time and a month from now will b the start and end time.
     */
    fun getUserEvents(
        caller: Context,
        startMillis: Long? = null,
        endMillis: Long? = null
    ): MutableCollection<PlannerEvent>? {

        if (calenderIds.isNotEmpty()) {
            return getEventsFromCalendars(caller, this.calenderIds, startMillis, endMillis)
        } else {
            Log.e(TAG, "getUserEvents: No calendar Ids")
            return null
        }

    }

    /**
     * Get all events of the specified google calendar id's from google calendar.
     */
    fun getEventsFromCalendars(caller: Context,
                               chosenCalendarIds: MutableCollection<Long>,
                               startMillis: Long? = null,
                               endMillis: Long? = null
    ) : MutableCollection<PlannerEvent>? {

        val contentResolver = caller.contentResolver
        val allEvents = mutableListOf<PlannerEvent>()
        val strongStartMillis = startMillis ?: getTodayTimeMillis()
        val strongEndMillis = endMillis ?: strongStartMillis + ONE_MONTH_MILLIS

        for (id in chosenCalendarIds) {
            val events =
                getCalendarEvents(contentResolver, id, strongStartMillis, strongEndMillis)
            allEvents.addAll(events)
        }
        return allEvents
    }

    fun setCalendarIds(chosenCalendarIds: List<Long>) {
        this.calenderIds = chosenCalendarIds.toMutableSet()
    }



    fun requestCalendarReadWritePermission(caller: Activity) {
        val permissionList: MutableList<String> = ArrayList()

        if (ContextCompat.checkSelfPermission(
                caller,
                Manifest.permission.WRITE_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(Manifest.permission.WRITE_CALENDAR)
        }

        if (ContextCompat.checkSelfPermission(
                caller,
                Manifest.permission.READ_CALENDAR
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(Manifest.permission.READ_CALENDAR)
        }

        if (permissionList.size > 0) {
            val permissionArray = arrayOfNulls<String>(permissionList.size)
            for (i in permissionList.indices) {
                permissionArray[i] = permissionList[i]
            }

            ActivityCompat.requestPermissions(
                caller,
                permissionArray,
                PERMISSION_REQUEST_CODE
            )

        }
    }

    fun haveCalendarReadPermissions(caller: Context): Boolean {
        var permissionCheck = ContextCompat.checkSelfPermission(
            caller,
            Manifest.permission.READ_CALENDAR
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    fun haveCalendarWritePermissions(caller: Context): Boolean {
        var permissionCheck = ContextCompat.checkSelfPermission(
            caller,
            Manifest.permission.WRITE_CALENDAR
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }


    fun haveCalendarReadWritePermissions(caller: Context): Boolean {

        var permissionCheck = ContextCompat.checkSelfPermission(
            caller,
            Manifest.permission.READ_CALENDAR
        )

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            permissionCheck = ContextCompat.checkSelfPermission(
                caller,
                Manifest.permission.WRITE_CALENDAR
            )

            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }

        return false
    }

}