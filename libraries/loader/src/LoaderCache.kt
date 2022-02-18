package featurea.loader

import featurea.config.ConfigContent
import featurea.rml.reader.RmlContent
import featurea.runtime.Container
import featurea.text.TextContent

fun Container.removeCachedFile(filePath: String) {
    val rmlContent: RmlContent = import()
    val textContent: TextContent = import()

    rmlContent.removeCachedRmlFile(filePath)
    textContent.removeCachedText(filePath)
}

fun Container.clearCaches() {
    val configContent: ConfigContent = import()
    val rmlContent: RmlContent = import()
    val textContent: TextContent = import()

    configContent.clearCache()
    rmlContent.clear()
    textContent.clearCache()
}
