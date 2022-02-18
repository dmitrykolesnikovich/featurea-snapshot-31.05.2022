package featurea.jvm

import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.Node.ATTRIBUTE_NODE
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import java.io.StringReader
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

operator fun Element.get(key: String, defaultValue: String? = null): String? {
    val value = getAttribute(key)
    if (value.isNullOrBlank()) return defaultValue
    return value
}

inline fun Element.forEachChildTag(action: (tag: Element) -> Unit) {
    for (index in 0 until childNodes.length) {
        val childNode = childNodes.item(index)
        if (childNode.nodeType == Node.ELEMENT_NODE) {
            childNode as Element
            action(childNode)
        }
    }
}

inline fun Element.forEachAttribute(action: (key: String, value: String) -> Unit) {
    for(index in 0 until attributes.length) {
        val node = attributes.item(index)
        if(node.nodeType == ATTRIBUTE_NODE) {
            val attr = node as Attr
            val key = attr.nodeName
            val value = attr.nodeValue
            action(key, value)
        }
    }
}

fun File.readXmlDocument(): Element {
    val builderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    builderFactory.isIgnoringElementContentWhitespace = false
    builderFactory.isCoalescing = true
    val builder: DocumentBuilder = builderFactory.newDocumentBuilder()
    val document: Document = builder.parse(FileInputStream(this))
    val documentTag = document.documentElement
    return documentTag
}

fun String.readXmlDocument(): Element {
    val builderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
    val builder: DocumentBuilder = builderFactory.newDocumentBuilder()
    val inputStream = InputSource(StringReader(this))
    val document: Document = builder.parse(inputStream)
    val documentTag = document.documentElement
    return documentTag
}
