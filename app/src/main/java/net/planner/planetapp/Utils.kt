package net.planner.planetapp

import android.util.Log
import net.planner.planetapp.viewmodels.InitialSettingsFragmentViewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private const val DATE_DATA_FORMAT_SHORT = "dd/MM H:mm"
private const val DATE_DATA_FORMAT = "dd/MM/yyyy H:mm"
private const val MONTH_DATA_FORMAT = "MMM dd,yyyy H:mm:ss"
private const val DAY_DATA_FORMAT = "dd/MM/yyyy"
private const val HOUR_DATA_FORMAT = "HH:mm" // T0 display as hour:minute AM/PM: hh.mm aa



const val SUNDAY = "Sun"
const val MONDAY = "Mon"
const val TUESDAY = "Tue"
const val WEDNESDAY = "Wed"
const val THURSDAY = "Thu"
const val FRIDAY = "Fri"
const val SATURDAY = "Sat"


fun getDate(timeMillis: Long): String {
    return getDateImpl(timeMillis, DATE_DATA_FORMAT)
}


fun getShortDate(timeMillis: Long): String {
    return getDateImpl(timeMillis, DATE_DATA_FORMAT_SHORT)
}

private fun getDateImpl(timeMillis: Long, format: String = DATE_DATA_FORMAT): String {
    val c = Calendar.getInstance()
    c.timeInMillis = timeMillis
    return SimpleDateFormat(format, Locale.getDefault()).format(c.time)
}


fun getDayDate(timeMillis: Long): String {
    val c = Calendar.getInstance()
    c.timeInMillis = timeMillis
    return SimpleDateFormat(DAY_DATA_FORMAT, Locale.getDefault()).format(c.time)
}

fun getHour(timeMillis: Long): String {
    val c = Calendar.getInstance()
    c.timeInMillis = timeMillis
    return SimpleDateFormat(HOUR_DATA_FORMAT, Locale.getDefault()).format(c.time)
}

fun getMillisFromDateAndTime(date: String): Long? {
    return SimpleDateFormat(DATE_DATA_FORMAT, Locale.getDefault()).parse(date)?.time
}

fun getMillisFromDate(date: String): Long? {
    return SimpleDateFormat(DAY_DATA_FORMAT, Locale.getDefault()).parse(date)?.time
}

fun getMillisFromHour(date: String): Long? {
    val dateFormat = SimpleDateFormat(HOUR_DATA_FORMAT, Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
    return dateFormat.parse(date)?.time
}

fun getNumberOfDaysSinceSunday(day: String): Int {
    return when(day) {
        SUNDAY -> 0
        MONDAY -> 1
        TUESDAY -> 2
        WEDNESDAY -> 3
        THURSDAY -> 4
        FRIDAY -> 5
        SATURDAY -> 6
        else -> -1
    }
}

fun getMillisSinceSunday(day: String): Long {
    val numDays = getNumberOfDaysSinceSunday(day)
    if (numDays == -1) {
        return -1L
    }
    return TimeUnit.DAYS.toMillis(numDays.toLong())
}

fun isAvgTaskInputValid(input: String): Boolean {
    try {
        val avgTaskHours = input.toDouble().toLong()
        val avgTaskMinutes = TimeUnit.HOURS.toMinutes(avgTaskHours)
        if (avgTaskMinutes < 30 || avgTaskMinutes > 6000) {
            return false
        } else {
            return true
        }
    } catch (e: Exception) {
        return false
    }
}

fun isPreferredTimeInputValid(input: String, avgTaskHours: Double): Boolean {
    try {
        val preferredSessionTimeHours = input.toDouble().toLong()
        val preferredSessionTimeMinutes = TimeUnit.HOURS.toMinutes(preferredSessionTimeHours)
        val avgTaskMinutes = TimeUnit.HOURS.toMinutes(avgTaskHours.toLong())
        if (preferredSessionTimeMinutes < 30 || preferredSessionTimeMinutes > avgTaskMinutes) {
            return false
        }
        return true
    } catch (e: Exception) {
        return false
    }
}

fun createTimeString(hour: Int, minutes: Int): String{
    var timeString = "$hour"
    if (hour < 10) {
        timeString = " 0" + hour.toString()
    }
    timeString += ":"
    if(minutes < 10) {
        timeString += "0"
    }
    timeString += minutes.toString()
    return timeString
}
