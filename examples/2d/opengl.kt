package featurea.examples.opengl

import featurea.app.bootstrapSandbox

fun main() = bootstrapSandbox {
    appendTest { testBlend() }
    appendTest { testCamera() }
    appendTest { testColors() }
    appendTest { testCoordinateSystem() }
    appendTest { testLines() }
}
