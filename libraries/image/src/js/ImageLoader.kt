package featurea.image

import featurea.System
import featurea.utils.normalizedPath
import featurea.opengl.OpenglImpl
import featurea.opengl.OpenglProxy
import featurea.opengl.TRUE
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import org.khronos.webgl.WebGLRenderingContext.Companion.RGBA
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.UNPACK_FLIP_Y_WEBGL
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_BYTE
import kotlin.coroutines.suspendCoroutine
import org.w3c.dom.Image as JsImage

actual class ImageLoader actual constructor(override val module: Module) : Component {

    private val gl: OpenglImpl = import(OpenglProxy) as OpenglImpl
    private val system: System = import()

    actual suspend fun loadImage(image: Image) {
        suspendCoroutine<Unit> { continuation ->
            val jsImage: JsImage = JsImage()
            val src: String = "${system.workingDir ?: "bundle"}/${image.spritesheet.spritePath}".normalizedPath
            jsImage.src = src
            jsImage.onload = {
                gl.context.pixelStorei(UNPACK_FLIP_Y_WEBGL, TRUE) // IMPORTANT flip
                gl.context.texImage2D(
                    target = TEXTURE_2D,
                    level = 0,
                    internalformat = RGBA,
                    format = RGBA,
                    type = UNSIGNED_BYTE,
                    source = jsImage
                )
                image.spritesheet.size.assign(jsImage.width, jsImage.height)
                continuation.resumeWith(Result.success(Unit))
            }
        }
    }

}
