package featurea.config

import featurea.content.contentTypes
import featurea.rml.reader.RmlContent
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.config") {
    "ConfigContent" to ConfigContent::class
    "ConfigReader" to ConfigReader::class

    static {
        val rmlContent: RmlContent = import()
        val configContent: ConfigContent = ConfigContent(rmlContent)
        provideComponent(configContent)
        provideComponent(ConfigReader(configContent))
    }

    contentTypes {
        "ConfigContentType" to ::ConfigContentType
    }
}
