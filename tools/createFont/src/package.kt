package featurea.createFont

import featurea.runtime.Artifact
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.script.Script
import featurea.jvm.userHomePath
import java.io.File

/*dependencies*/

val artifact = Artifact("featurea.createFont") {
    include(featurea.desktop.artifact)
    include(featurea.window.artifact)

    "FontCreator" to FontCreator::class
    "Docket" to ::Docket

    static {
        provideComponent(FontCreator())
    }
}

/*dockets*/

class Docket(override val module: Module) : Component, Script {

    private val fontCreator: FontCreator = import()

    override suspend fun executeAction(action: String, args: List<Any?>, isSuper: Boolean): Any {
        when (action) {
            "FontCreator.createFont" -> {
                val fntFile: File = File("$userHomePath/${args[0] as String}")
                val name: String = args[1] as String
                val size: Int = args[2] as Int
                val isBold: Boolean = args[3] as Boolean
                val isItalic: Boolean = args[4] as Boolean
                if (!fntFile.exists()) {
                    fontCreator.createFont(fntFile, name, size, isBold, isItalic)
                }
            }
        }
        return Unit
    }

}
