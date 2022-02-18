package featurea.exposed

import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime

fun Table.findColumnOrNull(name: String): Column<*>? {
    return columns.find { it.name == name }
}

fun DateTime.toKotlinInstant(): Instant {
    return Instant.fromEpochMilliseconds(millis)
}
