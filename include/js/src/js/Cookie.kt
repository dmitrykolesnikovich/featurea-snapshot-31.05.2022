// https://stackoverflow.com/a/24103596/909169

package featurea.js

import featurea.datetime.toUtcString
import kotlinx.browser.document
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

// Tue, 17 Aug 2021 12:10:56 GMT
@OptIn(ExperimentalTime::class)
fun setCookie(name: String, value: String?, days: Int = -1) {
    var expires = ""
    if (days != -1) {
        val date: Instant = Clock.System.now() + days.toDuration(DurationUnit.DAYS)
        expires = "; expires=" + date.toUtcString()
    }
    document.cookie = name + "=" + (value ?: "") + expires + "; path=/"
}

fun getCookie(name: String): String? {
    val nameEQ = "$name="
    val ca: List<String> = document.cookie.split(';')
    for (c in ca) {
        var c = c
        while (c.toCharArray().first() == ' ') c = c.substring(1, c.length)
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length)
    }
    return null
}

fun eraseCookie(name: String) {
    // document.cookie = "$name=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;"
    document.cookie = "$name=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/"
}
