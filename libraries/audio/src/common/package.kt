package featurea.audio

import featurea.content.contentTypes
import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder

/*dependencies*/

expect fun DependencyBuilder.includeExternals()

val artifact = Artifact("featurea.audio") {
    includeExternals()
    include(featurea.loader.artifact)
    include(featurea.audio.reader.artifact)

    "Audio" to ::Audio
    "AudioDelegate" to ::AudioDelegate
    "AudioEffect" to ::AudioEffect
    "AudioTrack" to ::AudioTrack

    contentTypes {
        "AudioContentType" to ::AudioContentType
    }
}
