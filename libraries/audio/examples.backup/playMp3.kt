package featurea.audio

import featurea.log
import featurea.runtime.*

fun playMp3() {
    val audio = Audio(Module(Runtime(), Container(DependencyRegistry.fromDependency(Artifact("featurea.audio")))))
    log("$audio: ${Resources.trumpetMp3}")
}
