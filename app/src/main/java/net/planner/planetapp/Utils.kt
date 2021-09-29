package net.planner.planetapp

import java.text.SimpleDateFormat
import java.util.*

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