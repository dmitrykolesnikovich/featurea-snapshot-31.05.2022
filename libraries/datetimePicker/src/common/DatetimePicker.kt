package featurea.datetimePicker

import featurea.runtime.Module
import featurea.runtime.Component

expect class DatetimePicker(module: Module) : Component {
    fun show(hours: Int, minutes: Int, change: (time: String) -> Unit)
}
