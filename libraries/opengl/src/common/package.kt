package featurea.opengl

import featurea.runtime.Artifact
import featurea.runtime.Delegate
import featurea.runtime.DependencyBuilder
import featurea.runtime.Proxy

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea.opengl") {
    includeExternals()
    include(featurea.window.artifact)

    "OpenglProxy" to OpenglProxy::class
}

class OpenglProxy(override var delegate: Opengl) : Proxy<Opengl> {
    companion object : Delegate<Opengl>(OpenglProxy::class)
}
