package featurea.image.reader

import featurea.Properties
import featurea.PropertyDelegate
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.image.reader") {
    include(featurea.spritesheet.artifact)

    "ImageReader" to ImageReader::class

    static {
        provideComponent(ImageReader(container = this))
    }
}

/*properties*/

var Properties.atlas: String by PropertyDelegate("atlas") { error("atlas") }
var Properties.pack: String by PropertyDelegate("pack") { error("pack") }
var Properties.texture: String by PropertyDelegate("texture") { error("texture") }
