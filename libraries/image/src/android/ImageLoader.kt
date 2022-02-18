package featurea.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLUtils
import featurea.System
import featurea.android.flip
import featurea.jvm.readInputStreamOrNull
import featurea.opengl.TEXTURE_2D
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import java.io.InputStream

actual class ImageLoader actual constructor(override val module: Module) : Component {

    private val system: System = import()

    actual suspend fun loadImage(image: Image) {
        val imagePath: String = image.spritesheet.spritePath
        val inputStream: InputStream = system.readInputStreamOrNull(imagePath) ?: throw error("imagePath: $imagePath")
        val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
        val flippedBitmap: Bitmap = bitmap.flip() // IMPORTANT flip
        GLUtils.texImage2D(TEXTURE_2D, 0, flippedBitmap, 0)
        flippedBitmap.recycle()
        bitmap.recycle()
        image.spritesheet.size.assign(bitmap.width, bitmap.height)
    }

}
