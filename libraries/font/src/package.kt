package featurea.font

import featurea.content.contentTypes
import featurea.utils.featureaDir
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.font") {
    includeContentRootWithConfig { "$featureaDir/libraries/font/res" }
    include(featurea.font.reader.artifact)
    include(featurea.image.artifact)

    contentTypes {
        "FontContentType" to ::FontContentType
    }
}
