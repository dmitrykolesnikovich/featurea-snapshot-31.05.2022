@file:JvmName("Time")

package featurea

import java.lang.System as JvmSystem
import java.text.SimpleDateFormat
import java.util.*

private val logDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS")

actual fun getTimeMillis(): Double = JvmSystem.currentTimeMillis().toDouble()

actual fun nowString(): String = logDateFormat.format(Date())
