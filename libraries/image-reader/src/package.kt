package featurea.image.reader

import featurea.Bundle
import featurea.Properties
import featurea.utils.PropertyDelegate
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

/*quickfix*/

/*quickfix*/

val Bundle.texturePack: MutableMap<String, String>
    get() {
        var texturePack = manifest.map["texturePack"] as MutableMap<String, String>?
        if (texturePack == null) {
            texturePack = mutableMapOf()
            manifest["texturePack"] = texturePack
        }
        return texturePack
    }