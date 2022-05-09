@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package featurea.utils

import platform.Foundation.*
import kotlin.native.concurrent.freeze

@SharedImmutable
private val numberFormatter = NSNumberFormatter().apply {
    locale = NSLocale.localeWithLocaleIdentifier("en_US")
    numberStyle = NSNumberFormatterDecimalStyle
    maximumFractionDigits = 12u
    minimumFractionDigits = 1u
    decimalSeparator = "."
    alwaysShowsDecimalSeparator = true
    usesGroupingSeparator = false
}

actual fun Double.toDoubleString(): String {
    initRuntimeIfNeeded()
    val result: String? = numberFormatter.stringFromNumber(NSNumber(this))
    return result?.freeze() ?: error("conversion failed: $this")
}
