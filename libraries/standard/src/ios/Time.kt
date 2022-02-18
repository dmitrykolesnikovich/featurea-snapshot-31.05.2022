package featurea

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.date
import platform.Foundation.timeIntervalSince1970

actual fun getTimeMillis(): Double = NSDate.date().timeIntervalSince1970 * 1000.0

@SharedImmutable
private val dateFormatter = NSDateFormatter().apply { setDateFormat("dd.MM.yyyy HH:mm:ss.SSS") }

actual fun nowString(): String = dateFormatter.stringFromDate(NSDate.date())