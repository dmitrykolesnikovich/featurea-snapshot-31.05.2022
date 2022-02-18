package featurea.image

import featurea.content.contentTypes
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.image") {
    include(featurea.content.artifact)
    include(featurea.image.reader.artifact)
    include(featurea.opengl.artifact)

    "ImageContent" to ::ImageContent
    "ImageLoader" to ::ImageLoader

    contentTypes {
        "ImageContentType" to ::ImageContentType
    }
}
