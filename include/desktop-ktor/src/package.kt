package featurea.ktor

import featurea.runtime.Artifact

val artifact = Artifact("featurea.ktor") {
    "SocketProtocol" to SocketProtocol::class
}