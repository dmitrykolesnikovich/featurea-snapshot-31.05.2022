package featurea.net

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun getDocumentOrNull(url: String): Document? {
    return Jsoup.connect(url).get()
}
