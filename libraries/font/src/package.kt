package featurea.font

import featurea.content.contentTypes
import featurea.featureaDir
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.font") {
    includeContentRootWithConfig { "$featureaDir/engine/libraries/font/res" }
    include(featurea.font.reader.artifact)
    include(featurea.image.artifact)

    contentTypes {
        "FontContentType" to ::FontContentType
    }
}
