package featurea.webSocket

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.webSocket") {
    include(featurea.content.artifact)

    "WebSocket" to ::WebSocket
}
