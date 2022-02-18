package featurea.text

import featurea.System
import featurea.content.contentTypes
import featurea.runtime.Artifact

val artifact = Artifact("featurea.text") {
    include(featurea.text.reader.artifact)

    "TextContent" to TextContent::class

    contentTypes {
        "TextContentType" to ::TextContentType
    }

    static {
        val system: System = import()
        provideComponent(TextContent(system))
    }
}
