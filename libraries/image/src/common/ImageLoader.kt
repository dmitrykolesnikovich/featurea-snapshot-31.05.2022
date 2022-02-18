package featurea.image

import featurea.runtime.Component
import featurea.runtime.Module

expect class ImageLoader(module: Module) : Component {
    suspend fun loadImage(image: Image)
}
