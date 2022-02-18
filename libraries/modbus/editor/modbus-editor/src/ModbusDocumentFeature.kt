package featurea.modbus.editor

import featurea.content.ResourceTag
import featurea.runtime.Module
import featurea.studio.editor.EditorFeature
import kotlin.math.max
import kotlin.math.min

private const val connectionRetriesCountMax: Int = 100
private const val channelUpdateIntervalMin: Int = 500

class ModbusDocumentFeature(module: Module) : EditorFeature(module) {

    override fun filterAttribute(rmlTag: ResourceTag, key: String, value: String): String {
        if (rmlTag.name == "Channel" && key == "address" && value.isNotEmpty()) {
            rmlTag.attributes.remove("scriptType")
            rmlTag.attributes.remove("scriptTimeout")
        }
        if (rmlTag.name == "Connection" && key == "retriesCount") {
            val intValue = value.toIntOrNull()
            if (intValue != null) return min(connectionRetriesCountMax, intValue).toString()
        }
        if (rmlTag.name == "Channel" && key == "updateInterval") {
            val intValue = value.toIntOrNull()
            if (intValue != null) return max(channelUpdateIntervalMin, intValue).toString()
        }
        return value
    }

}
