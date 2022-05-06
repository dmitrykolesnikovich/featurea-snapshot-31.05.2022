package featurea.datetime

import featurea.utils.log
import featurea.utils.toUpperCaseFirst
import kotlinx.datetime.*
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

fun convertMillisecondsToInstant(milliseconds: Long, timeZone: TimeZone = TimeZone.UTC): Instant {
    val instant: Instant = Instant.fromEpochMilliseconds(milliseconds)
    val dateTime: LocalDateTime = instant.toLocalDateTime(timeZone)
    return dateTime.toInstant()
}

fun convertSecondsToInstant(seconds: Double, timeZone: TimeZone = TimeZone.UTC): Instant {
    val instant: Instant = Instant.fromEpochSeconds(seconds.toLong())
    val dateTime: LocalDateTime = instant.toLocalDateTime(timeZone)
    return dateTime.toInstant()
}

fun LocalDateTime.toInstant(): Instant {
    return toInstant(timeZone = TimeZone.UTC)
}

// Tue, 17 Aug 2021 12:10:56 GMT
fun Instant.toUtcString(timeZone: TimeZone = TimeZone.UTC): String {
    val time: LocalDateTime = toLocalDateTime(timeZone)
    val day: String = time.dayOfWeek.name.substring(0, 3).toLowerCase().toUpperCaseFirst(1)
    val month: String = time.month.name.substring(0, 3).toLowerCase().toUpperCaseFirst(1)
    return "$day, ${time.dayOfMonth} $month ${time.yearText} ${time.hourText}:${time.minuteText}:${time.secondText} GMT"
}

// 01234567890123456789012
// 02.04.2021 00:28:06.479
fun String.toCloudDateTime(): LocalDateTime {
    val DD = substring(0, 2).toInt()
    val MM = substring(3, 5).toInt()
    val YYYY = substring(6, 10).toInt()
    val HH = substring(11, 13).toInt()
    val mm = substring(14, 16).toInt()
    val ss = substring(17, 19).toInt()
    return LocalDateTime(year = YYYY, monthNumber = MM, dayOfMonth = DD, hour = HH, minute = mm, second = ss)
}

fun nowInstant(timeZone: TimeZone = TimeZone.UTC): Instant {
    val dateTime: LocalDateTime = Clock.System.now().toLocalDateTime(timeZone)
    return dateTime.toInstant()
}

fun zeroSecondOfToday(timeZone: TimeZone = TimeZone.UTC): Instant {
    val now: LocalDateTime = nowInstant().toLocalDateTime(timeZone)
    val dateTime = LocalDateTime(year = now.year, monthNumber = now.monthNumber, dayOfMonth = now.dayOfMonth, 0, 0, 0)
    val instant: Instant = dateTime.toInstant()
    // log("[zeroSecondOfToday] instant: $instant")
    return instant
}

@OptIn(ExperimentalTime::class)
fun zeroSecondOfTomorrow(): Instant {
    return zeroSecondOfToday() + 1.toDuration(DurationUnit.DAYS)
}

fun Instant.formatToDayMonthYearHourMinuteSecondInCurrentSystemTimeZone(): String {
    return formatToDayMonthYearHourMinuteSecond(TimeZone.currentSystemDefault())
}

fun Instant.formatToDayMonthYearHourMinuteSecond(timeZone: TimeZone = TimeZone.UTC): String {
    val dateTime: LocalDateTime = toLocalDateTime(timeZone)
    return "${dateTime.dayText}.${dateTime.monthText}.${dateTime.yearText} ${dateTime.hourText}:${dateTime.minuteText}:${dateTime.secondText}"
}

fun Instant.formatToMinuteSecondInCurrentSystemTimeZone(): String {
    return formatToMinuteSecond(TimeZone.currentSystemDefault())
}

fun Instant.formatToMinuteSecond(timeZone: TimeZone = TimeZone.UTC): String {
    val dateTime: LocalDateTime = toLocalDateTime(timeZone)
    return "${dateTime.minuteText}:${dateTime.secondText}"
}

fun Instant.formatToHourMinuteInCurrentSystemTimeZone(): String {
    return formatToHourMinute(TimeZone.currentSystemDefault())
}

fun Instant.formatToHourMinute(timeZone: TimeZone = TimeZone.UTC): String {
    val dateTime: LocalDateTime = toLocalDateTime(timeZone)
    return "${dateTime.hourText}:${dateTime.minuteText}"
}

fun Instant.formatToDayMonthInCurrentSystemTimeZone(): String {
    return formatToDayMonth(TimeZone.currentSystemDefault())
}

fun Instant.formatToDayMonth(timeZone: TimeZone = TimeZone.UTC): String {
    val dateTime: LocalDateTime = toLocalDateTime(timeZone)
    return "${dateTime.dayText}.${dateTime.monthText}"
}

fun Instant.formatToDayMonthYearHourMinuteInCurrentSystemTimeZone(): String {
    return formatToDayMonthYearHourMinute(TimeZone.currentSystemDefault())
}

fun Instant.formatToDayMonthYearHourMinute(timeZone: TimeZone = TimeZone.UTC): String {
    val dateTime: LocalDateTime = toLocalDateTime(timeZone)
    return "${dateTime.dayText}.${dateTime.monthText}.${dateTime.yearText} ${dateTime.hourText}:${dateTime.minuteText}"
}

fun Instant.formatToHourMinuteSecondInCurrentSystemTimeZone(): String {
    return formatToHourMinuteSecond(TimeZone.currentSystemDefault())
}

fun Instant.formatToHourMinuteSecond(timeZone: TimeZone = TimeZone.UTC): String {
    val dateTime: LocalDateTime = toLocalDateTime(timeZone)
    return "${dateTime.hourText}:${dateTime.minuteText}:${dateTime.secondText}"
}

val LocalDateTime.yearText: String get() = year.toString()

val LocalDateTime.monthText: String
    get() {
        if (monthNumber < 10) return "0${monthNumber}"
        return monthNumber.toString()
    }

val LocalDateTime.dayText: String
    get() {
        if (dayOfMonth < 10) return "0${dayOfMonth}"
        return dayOfMonth.toString()
    }

val LocalDateTime.hourText: String
    get() {
        if (hour < 10) return "0${hour}"
        return hour.toString()
    }

val LocalDateTime.minuteText: String
    get() {
        if (minute < 10) return "0${minute}"
        return minute.toString()
    }

val LocalDateTime.secondText: String
    get() {
        if (second < 10) return "0${second}"
        return second.toString()
    }
