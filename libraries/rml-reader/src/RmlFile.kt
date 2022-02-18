package featurea.rml.reader

import featurea.content.ResourceTag
import featurea.content.ResourceSchema

class RmlFile {

    lateinit var rmlTag: ResourceTag
        private set
    lateinit var rmlSchema: ResourceSchema
        private set
    lateinit var text: String
        private set
    lateinit var filePath: String
        private set
    lateinit var packageId: String
        private set

    suspend fun init(text: String, filePath: String, initRmlSchema: suspend RmlFile.() -> ResourceSchema) {
        this.text = text
        this.filePath = filePath
        this.rmlTag = RmlParser.parseRmlSource(text, filePath)
        this.packageId = rmlTag.packageId
        this.rmlSchema = initRmlSchema()
    }

}
