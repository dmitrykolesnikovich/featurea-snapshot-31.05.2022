package featurea.image

import de.matthiasmann.twl.utils.PNGDecoder
import featurea.System
import featurea.desktop.createGraphicsFlipped
import featurea.jvm.BufferFactory.createByteBuffer
import featurea.jvm.isWindows
import featurea.jvm.readInputStreamOrNull
import featurea.utils.log
import featurea.opengl.*
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import org.lwjgl.BufferUtils
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_3BYTE_BGR
import java.awt.image.BufferedImage.TYPE_4BYTE_ABGR
import java.io.InputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO

actual class ImageLoader actual constructor(override val module: Module) : Component {

    private val gl: OpenglImpl = import(OpenglProxy)
    private val system: System = import()

    @Suppress("BlockingMethodInNonBlockingContext")
    actual suspend fun loadImage(image: Image) {
        val imagePath: String = image.spritesheet.spritePath
        // log("[ImageLoader.kt] loadImage: $imagePath")
        val inputStream: InputStream? = system.readInputStreamOrNull(imagePath)
        if (inputStream == null) {
            println("Image not found: $imagePath")
            return
        }
        try {
            if (imagePath.endsWith(".png")) {
                val pngDecoder: PNGDecoder = PNGDecoder(inputStream)
                val width: Int = pngDecoder.width
                val height: Int = pngDecoder.height
                val alignment: Int = 1
                val stride: Int = 4 * alignment * width
                val buffer: ByteBuffer = createByteBuffer(stride * height)
                pngDecoder.decodeFlipped(buffer, stride, PNGDecoder.Format.RGBA) // IMPORTANT flip
                buffer.flip()
                gl.context.glPixelStorei(UNPACK_ALIGNMENT, alignment)
                gl.context.glPixelStorei(PACK_ALIGNMENT, 1)
                gl.context.glTexImage2D(TEXTURE_2D, 0, RGBA, width, height, 0, RGBA, UNSIGNED_BYTE, buffer)
                image.spritesheet.size.assign(width.toFloat(), height.toFloat())
            } else if (imagePath.endsWith(".jpeg") || imagePath.endsWith(".jpg")) {
                val bufferedImage: BufferedImage = ImageIO.read(inputStream)
                val imageWidth: Int = bufferedImage.width
                val imageHeight: Int = bufferedImage.height
                val numComponents: Int = bufferedImage.colorModel.numComponents
                val imageType: Int = if (numComponents == 3) TYPE_3BYTE_BGR else TYPE_4BYTE_ABGR
                val bufferedImageFlipped: BufferedImage = BufferedImage(imageWidth, imageHeight, imageType)
                val graphics: Graphics2D = bufferedImageFlipped.createGraphicsFlipped() // IMPORTANT flip
                graphics.drawImage(bufferedImage, 0, 0, null)
                val imageSrc: ByteArray = ByteArray(numComponents * imageWidth * imageHeight)
                bufferedImageFlipped.raster.getDataElements(0, 0, imageWidth, imageHeight, imageSrc)
                val buffer: ByteBuffer = BufferUtils.createByteBuffer(imageSrc.size)
                buffer.put(imageSrc)
                buffer.flip()
                gl.context.glPixelStorei(UNPACK_ALIGNMENT, 1)
                gl.context.glPixelStorei(PACK_ALIGNMENT, 1)
                gl.context.glTexImage2D(TEXTURE_2D, 0, 3, imageWidth, imageHeight, 0, RGB, UNSIGNED_BYTE, buffer)
                image.spritesheet.size.assign(imageWidth.toFloat(), imageHeight.toFloat())
            }
        } finally {
            inputStream.close()
        }
    }

}
