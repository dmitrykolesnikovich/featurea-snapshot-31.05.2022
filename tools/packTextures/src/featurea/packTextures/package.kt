package featurea.packTextures

import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.texturePacker") {
    "TexturePacker" to TexturePacker::class

    static {
        provideComponent(TexturePacker())
    }
}
