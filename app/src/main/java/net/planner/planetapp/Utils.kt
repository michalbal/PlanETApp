package net.planner.planetapp

import java.text.SimpleDateFormat
import java.util.*

private const val DATE_DATA_FORMAT_SHORT = "dd/MM H:mm"
private const val DATE_DATA_FORMAT = "dd/MM/yyyy H:mm:ss"
private const val MONTH_DATA_FORMAT = "MMM dd,yyyy H:mm:ss"
private const val DAY_DATA_FORMAT = "MMM dd"
private const val HOUR_DATA_FORMAT = "HH:mm" // T0 display as hour:minute AM/PM: hh.mm aa


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


fun getDay(timeMillis: Long): String {
    val c = Calendar.getInstance()
    c.timeInMillis = timeMillis
    return SimpleDateFormat(DAY_DATA_FORMAT, Locale.getDefault()).format(c.time)
}

fun getHour(timeMillis: Long): String {
    val c = Calendar.getInstance()
    c.timeInMillis = timeMillis
    return SimpleDateFormat(HOUR_DATA_FORMAT, Locale.getDefault()).format(c.time)
}