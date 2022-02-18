package featurea.studio.project.components

import featurea.config.ConfigFile
import featurea.config.importConfig
import featurea.desktop.jfx.FSMenuBar
import featurea.desktop.jfx.removeAllTrailingSeparatorMenuItems
import featurea.runtime.Container
import featurea.splitAndTrim

fun Container.initProjectMenuBar(): FSMenuBar {
    val menuBar: FSMenuBar = FSMenuBar()
    val configFile: ConfigFile = importConfig("projectMenuBar").files.lastOrNull() ?: return menuBar
    val lines: List<String> = configFile.source.lines().filter { !it.startsWith("#") }
    lateinit var firstTitle: String
    for (line in lines) {
        if (line.isEmpty()) {
            menuBar.findMenuItem(firstTitle, "")
        } else {
            val (key, value) = line.split("=")
            val titles: List<String> = value.splitAndTrim(",")
            firstTitle = titles.first()
            when {
                key.endsWith("-CheckMenuItem") -> menuBar.findCheckMenuItem(*titles.toTypedArray())
                else -> menuBar.findMenuItem(*titles.toTypedArray())
            }
        }
    }
    menuBar.removeAllTrailingSeparatorMenuItems()
    return menuBar
}
