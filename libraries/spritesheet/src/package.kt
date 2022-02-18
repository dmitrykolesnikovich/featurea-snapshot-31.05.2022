package featurea.spritesheet

import featurea.Properties
import featurea.PropertyDelegate
import featurea.System
import featurea.SystemPropertyDelegate
import featurea.runtime.Artifact

/*dependencies*/

val artifact = Artifact("featurea.spritesheet") {
    include(featurea.text.artifact)

    "SpritesheetCache" to SpriteCache::class
    "SpritesheetReader" to SpritesheetReader::class

    static {
        provideComponent(SpriteCache(container = this))
        provideComponent(SpritesheetReader(container = this))
    }
}

/*properties*/

var Properties.fps: String by PropertyDelegate("fps") { error("fps") }
var Properties.frameCount: Int by PropertyDelegate("frameCount") { error("frameCount") }
var Properties.frames: List<String> by PropertyDelegate("frames") { error("frames") }
var Properties.loopCount: Int by PropertyDelegate("loopCount") { 0 }
var System.useTexturePack: Boolean by SystemPropertyDelegate("useTexturePack") { false }
