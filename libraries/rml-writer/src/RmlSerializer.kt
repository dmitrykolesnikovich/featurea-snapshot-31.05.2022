package featurea.rml.writer

import featurea.content.ResourceTag
import featurea.rml.isLeaf
import java.io.File

// serialize `rmlTag` to `rmlFile`
object RmlSerializer {

    fun serialize(rmlTag: ResourceTag, rmlFile: File) {
        val stringBuffer = StringBuffer()
        traverseResourceTagRecursively(rmlTag, stringBuffer, 0)
        rmlFile.writeText(stringBuffer.toString())
    }

    /*internals*/

    private fun traverseResourceTagRecursively(resourceTag: ResourceTag, stringBuffer: StringBuffer, indentation: Int) {
        // 1. open tag
        repeat(indentation) { stringBuffer.append("    ") }
        stringBuffer.append("<${resourceTag.name}")

        // 2. attributes
        for ((key, value) in resourceTag.attributes) {
            val normalized = value.replace("\n", "\\n") // quickfix todo improve
            stringBuffer.append(" ${key}=\"${normalized}\"")
        }

        if (resourceTag.isLeaf()) {
            stringBuffer.append("/>")
        } else {
            stringBuffer.append(">")

            // 3. children
            for (childResourceTag in resourceTag.children) {
                stringBuffer.append("\n") // todo replace with lineSeparator
                traverseResourceTagRecursively(childResourceTag, stringBuffer, indentation + 1)
            }

            // 4. close tag
            stringBuffer.append("\n") // todo replace with lineSeparator
            repeat(indentation) { stringBuffer.append("    ") }
            stringBuffer.append("</${resourceTag.name}>")
        }
    }

}
