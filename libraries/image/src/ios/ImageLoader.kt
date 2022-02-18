package featurea.image

import cnames.structs.CGContext
import featurea.System
import featurea.opengl.*
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import platform.CoreGraphics.*
import platform.UIKit.UIImage
import platform.gles.glTexImage2D

// https://github.com/MihaiDamian/Cube-transition-example/blob/master/OpenGLViews/TextureAtlas.m
@ExperimentalUnsignedTypes
actual class ImageLoader actual constructor(override val module: Module) : Component {

    private val gl: Opengl = import(OpenglProxy)
    private val system: System = import()

    actual suspend fun loadImage(image: Image) {
        val imagePath: String = image.spritesheet.spritePath
        val imageView: UIImage = UIImage.imageNamed("assets/$imagePath")
            ?: UIImage.imageNamed("${system.workingDir}/$imagePath")
            ?: error("imagePath: $imagePath")
        val width: ULong = CGImageGetWidth(imageView.CGImage)
        val height: ULong = CGImageGetHeight(imageView.CGImage)
        val bitmapContext: CPointer<CGContext>? = CGBitmapContextCreate(
            data = null,
            width = width,
            height = height,
            bitsPerComponent = 8u,
            bytesPerRow = 4u * width,
            space = CGColorSpaceCreateDeviceRGB(),
            bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
        )

        // >> flip texture https://www.codetd.com/en/article/6566239
        val rect: CValue<CGRect> = CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble())
        CGContextTranslateCTM(bitmapContext, 0.0, height.toDouble())
        CGContextScaleCTM(bitmapContext, 1.0, -1.0);
        CGContextDrawImage(bitmapContext, rect, imageView.CGImage)
        // <<

        val data: CPointer<out CPointed>? = CGBitmapContextGetData(bitmapContext)
        gl.pixelStore(UNPACK_ALIGNMENT, 1)
        gl.pixelStore(PACK_ALIGNMENT, 1)
        glTexImage2D(
            target = TEXTURE_2D.toUInt(),
            level = 0,
            internalformat = RGBA,
            width = width.toInt(),
            height = height.toInt(),
            border = 0,
            format = RGBA.toUInt(),
            type = UNSIGNED_BYTE.toUInt(),
            pixels = data
        )
        CGContextRelease(bitmapContext)
        image.spritesheet.size.assign(width.toFloat(), height.toFloat())
    }

}
