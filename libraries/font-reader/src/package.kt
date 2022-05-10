package featurea.font.reader

import featurea.Properties
import featurea.runtime.Artifact
import featurea.utils.PropertyDelegate

/*dependencies*/

val artifact = Artifact("featurea.font.reader") {
    include(featurea.image.reader.artifact)

    "FontReader" to FontReader::class

    static {
        provideComponent(FontReader(staticModule))
    }
}

/*properties*/

var Properties.fnt: String by PropertyDelegate("fnt") { error("fnt") }
var Properties.png: String by PropertyDelegate("png") { error("png") }
