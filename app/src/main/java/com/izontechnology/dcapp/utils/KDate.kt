package com.izontechnology.dcapp.utils

import android.text.format.DateUtils
import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

const val API_DATE_FORMAT = "dd-MM-yyyy"
const val API_RETURN_DATE_FORMAT = "yyyy-MM-dd"
const val API_RETURN_UPDATE_FORMAT = "yyyy-MM-dd HH:mm:ss"// 2022-07-13T12:15:57Z
const val DISPLAY_DATE_FORMAT = "dd-MM-yyyy"
const val CREATED_ON_DATE_FORMAT = "dd-MM-yyyy"
const val CREDIT_CARD_FORMAT = "MM/yyyy"

fun String?.toDate(withFormat: String = API_DATE_FORMAT): Date {
    val dateFormat = SimpleDateFormat(withFormat)
    var convertedDate = Date()
    try {
        convertedDate = this?.let { dateFormat.parse(it) } as Date
    } catch (e: ParseException) {
        e.printStackTrace()
        return convertedDate
    }
    return convertedDate
}

fun Long.getDate(withFormat: String = API_RETURN_UPDATE_FORMAT) :String {
    try {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = this
        val dateFormat = SimpleDateFormat(withFormat, Locale.US)
        val date = dateFormat.format(calendar.time).toString()
        return date
    }catch (e:Exception){
        return ""
    }

}

fun todayDate(withFormat: String = API_DATE_FORMAT): String {
    val dateFormat = SimpleDateFormat(withFormat, Locale.US)
    val convertedDate = Date()
    var date: String = ""
    try {
        date = dateFormat.format(convertedDate)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return date
}

fun Date.toDate(withFormat: String = "dd-MM-yyyy"): Date {
    val dateFormat = SimpleDateFormat(withFormat, Locale.US)
    var convertedDate = Date()
    try {
        convertedDate = dateFormat.format(this).toDate()
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return convertedDate
}

fun String?.toAPIDateFormat(withFormat: String = DISPLAY_DATE_FORMAT): String {
    val dateFormat = SimpleDateFormat(withFormat, Locale.US)
    val apiateFormat = SimpleDateFormat(API_DATE_FORMAT, Locale.US)
    var date: String = ""
    if (this != null) {
        val convertedDate = dateFormat.parse(this) as Date
        try {
            date = apiateFormat.format(convertedDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    return date
}

fun String?.toDisplayDateFormat(withFormat: String = API_DATE_FORMAT, displayFormat: String = DISPLAY_DATE_FORMAT, locale: Locale? = Locale.getDefault()): String? {
    val dateFormat = SimpleDateFormat(withFormat, locale)
    val apiateFormat = SimpleDateFormat(displayFormat, locale)
    var date: String? = null
    if (this != null) {
        try {
            val convertedDate = dateFormat.parse(this) as Date
            date = apiateFormat.format(convertedDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    return date
}

fun String?.toDisplayDateHijriFormat(withFormat: String = API_DATE_FORMAT): String? {
    val dateFormat = SimpleDateFormat(withFormat, Locale.US)
    val apiateFormat = SimpleDateFormat(DISPLAY_DATE_FORMAT)
    var date: String = ""
    if (this != null) {
        val convertedDate = dateFormat.parse(this) as Date
        try {
            date = apiateFormat.format(convertedDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
    return date
}

/**
 * Return ordinal suffix (e.g. 'st', 'nd', 'rd', or 'th') for a given number
 *
 * @param value
 * a number
 * @return Ordinal suffix for the given number
 */
fun getOrdinalSuffix(value: Int?): String {
    val hunRem = value?.rem(100)
    val tenRem = value?.rem(10)
    return if (tenRem?.let { hunRem?.minus(it) } ?: 0 == 10) {
        "th"
    } else when (tenRem) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

// Converts current date to proper provided format
fun Date.convertTo(format: String = "dd MMM, yyyy"): String? {
    var dateStr: String? = ""
    val df = SimpleDateFormat(format, Locale.US)
    try {
        dateStr = df.format(this)
    } catch (ex: Exception) {
        Log.d("date", ex.toString())
    }
    return dateStr
}

fun Date.convertToWithComma(format: String = "dd MMM, yyyy"): String? {
    var dateStr: String? = ""
    val df = SimpleDateFormat(format, Locale.US)
    try {
        dateStr = df.format(this)
    } catch (ex: Exception) {
        Log.d("date", ex.toString())
    }
    return dateStr
}

// Converts current date to Calendar
fun Date.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal
}

fun getDate(years: Int): Date {
    val cal = Calendar.getInstance()
    cal.add(Calendar.YEAR, -years) // to get previous year add -1
    cal.add(Calendar.DAY_OF_YEAR, 1)
    return cal.time
}

fun getCalendar(years: Int): Calendar {
    val cal = Calendar.getInstance()
    cal.add(Calendar.YEAR, -years) // to get previous year add -1
    return cal
}

fun Date.isFuture(): Boolean {
    return !Date().before(this)
}

fun Date.isPast(): Boolean {
    return Date().before(this)
}

fun Date.isToday(): Boolean {
    return DateUtils.isToday(this.time)
}

fun Date.getTime(format: String = "HH:mm"): String? {
    var dateStr: String? = ""
    val df = SimpleDateFormat(format, Locale.US)
    try {
        dateStr = df.format(this)
    } catch (ex: Exception) {
        Log.d("date", ex.toString())
    }
    return dateStr
}

fun Date.getUTCTime(format: String = "HH:mm"): String? {
    var dateStr: String? = ""
    val df = SimpleDateFormat(format, Locale.US)
    try {
        df.timeZone = TimeZone.getTimeZone("UTC")
        dateStr = df.format(this)
    } catch (ex: Exception) {
        Log.d("date", ex.toString())
    }
    return dateStr
}

fun Date.isYesterday(): Boolean {
    return DateUtils.isToday(this.time + DateUtils.DAY_IN_MILLIS)
}

fun Date.isTomorrow(): Boolean {
    return DateUtils.isToday(this.time - DateUtils.DAY_IN_MILLIS)
}

fun Date.today(): Date {
    return Date()
}

fun Date.yesterday(): Date {
    val cal = this.toCalendar()
    cal.add(Calendar.DAY_OF_YEAR, -1)
    return cal.time
}

fun Date.tomorrow(): Date {
    val cal = this.toCalendar()
    cal.add(Calendar.DAY_OF_YEAR, 1)
    return cal.time
}

fun Date.hour(): Int {
    return this.toCalendar().get(Calendar.HOUR)
}

fun Date.minute(): Int {
    return this.toCalendar().get(Calendar.MINUTE)
}

fun Date.second(): Int {
    return this.toCalendar().get(Calendar.SECOND)
}

fun Date.month(): Int {
    return this.toCalendar().get(Calendar.MONTH) + 1
}

fun Date.monthName(locale: Locale? = Locale.getDefault()): String {
    return this.toCalendar().getDisplayName(Calendar.MONTH, Calendar.LONG, locale)
}

fun Date.year(): Int {
    return this.toCalendar().get(Calendar.YEAR)
}

fun Date.day(): Int {
    return this.toCalendar().get(Calendar.DAY_OF_MONTH)
}

fun Date.dayOfWeek(): Int {
    return this.toCalendar().get(Calendar.DAY_OF_WEEK)
}

fun Date.dayOfWeekName(locale: Locale? = Locale.getDefault()): String {
    return this.toCalendar().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale)
}

fun Date.dayOfYear(): Int {
    return this.toCalendar().get(Calendar.DAY_OF_YEAR)
}

fun Date.monthsBetweenDates(endDate: Date): Int {
    val start = Calendar.getInstance()
    start.time = this
    val end = Calendar.getInstance()
    end.time = endDate
    var monthsBetween = 0
    var dateDiff = end.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH)
    if (dateDiff < 0) {
        val borrrow = end.getActualMaximum(Calendar.DAY_OF_MONTH)
        dateDiff = end.get(Calendar.DAY_OF_MONTH) + borrrow - start.get(Calendar.DAY_OF_MONTH)
        monthsBetween--
        if (dateDiff > 0) {
            monthsBetween++
        }
    } else {
        monthsBetween++
    }
    monthsBetween += end.get(Calendar.MONTH) - start.get(Calendar.MONTH)
    monthsBetween += (end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) * 12
    return monthsBetween
}

fun String.hasTime(): Boolean {
    val endDate = this.toDate("dd/MM/yyyy hh:mm a")
    val currentDate = Date()
    val duration = endDate.time - currentDate.time
    return duration > 0
}

fun Long.dateDifferanceDays(): Long {
    val msDiff: Long = Math.abs(Calendar.getInstance().timeInMillis - this)
    return TimeUnit.DAYS.convert(msDiff, TimeUnit.MILLISECONDS) + 1
}