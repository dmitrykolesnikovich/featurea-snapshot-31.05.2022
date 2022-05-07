package featurea.font.reader

import featurea.Properties
import featurea.utils.PropertyDelegate
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.font.reader") {
    include(featurea.image.reader.artifact)

    "FontReader" to FontReader::class

    static {
        provideComponent(FontReader(container = this))
    }
}

/*properties*/

var Properties.fnt: String by PropertyDelegate("fnt") { error("fnt") }
var Properties.png: String by PropertyDelegate("png") { error("png") }
