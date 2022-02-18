package featurea.content

import featurea.Properties
import featurea.PropertyDelegate
import featurea.System
import featurea.SystemPropertyDelegate
import featurea.runtime.Artifact
import featurea.runtime.DependencyBuilder
import featurea.runtime.Plugin
import featurea.runtime.install

/*dependencies*/

val artifact = Artifact("featurea.content") {
    include(featurea.artifact)

    "Content" to Content::class
    "ContentTypeRegistry" to ::ContentTypeRegistry

    static {
        provideComponent(Content(container = this))
    }
}

/*properties*/

var Properties.mainDocument: String by PropertyDelegate("mainDocument") { "main" }
var Properties.mainProject: String by PropertyDelegate("mainProject") { error("mainProject") }
val Properties.packageId: String by PropertyDelegate("package") { error("package") }
val Properties.resources: String by PropertyDelegate("resources") { "" }
var System.rmlExtensions: List<String> by SystemPropertyDelegate("rmlExtensions") { listOf("project", "rml") }
var System.textExtensions: List<String> by SystemPropertyDelegate("textExtensions") { listOf("c", "kts") }
