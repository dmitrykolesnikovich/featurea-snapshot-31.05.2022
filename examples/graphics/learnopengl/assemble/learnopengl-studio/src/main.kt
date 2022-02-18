package featurea.examples.learnopengl.studio

import featurea.runtime.import
import featurea.studio.home.bootstrap
import featurea.studio.home.Studio

fun main() = bootstrap(artifact) {
    val studio: Studio = Studio(packageId = "learnopengl", delegate = import<LearnopenglStudioDelegate>())
}
