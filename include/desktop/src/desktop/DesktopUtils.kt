@file:JvmName("DesktopUtils")

package featurea.desktop

import featurea.utils.toColor
import featurea.jvm.copyTo
import featurea.utils.Property
import featurea.jvm.userHomePath
import featurea.utils.Color
import javafx.application.Platform
import javafx.embed.swing.SwingNode
import java.awt.Graphics2D
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.*
import javax.swing.JComponent
import javafx.scene.paint.Color as JfxColor
import java.lang.System as JvmSystem

val featureaCachePath: String = "$userHomePath/.featurea/cache/"
val featureaCacheDir: File = File(featureaCachePath)

val fileComparator: Comparator<File> = Comparator { file1, file2 ->
    // IMPORTANT fonts located at the very end of assets.properties generated file
    val hasFontExtensionFilePath1 = file1.name.endsWith(".fnt")
    val hasFontExtensionFilePath2 = file2.name.endsWith(".fnt")
    if (hasFontExtensionFilePath1 && !hasFontExtensionFilePath2) {
        return@Comparator 1
    }
    if (!hasFontExtensionFilePath1 && hasFontExtensionFilePath2) {
        -1
    } else file1.compareTo(file2)
}

val azFileComparator: Comparator<File> = Comparator { file1, file2 ->
    val extension1 = file1.extension
    val extension2 = file2.extension
    val extensionsCompareResultResult = extension1.compareTo(extension2)
    if (extensionsCompareResultResult != 0) {
        return@Comparator extensionsCompareResultResult
    }
    try {
        val index1 = Integer.parseInt(file1.nameWithoutExtension)
        val index2 = Integer.parseInt(file2.nameWithoutExtension)
        return@Comparator index1.compareTo(index2)
    } catch (e: Throwable) {
        return@Comparator fileComparator.compare(file1, file2)
    }
}

@Throws(IOException::class)
fun BufferedInputStream.newInputStream(): InputStream {
    mark(Integer.MAX_VALUE)
    val outputStream = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var len: Int = read(buffer)
    while (len > -1) {
        outputStream.write(buffer, 0, len)
        len = read(buffer)
    }
    reset()
    outputStream.flush()
    return ByteArrayInputStream(outputStream.toByteArray())
}

suspend fun File.copyInputStreamToFile(inputStream: InputStream) {
    inputStream.use { input ->
        this.outputStream().use { outputStream ->
            input.copyTo(outputStream)
        }
    }
}

fun BufferedInputStream.reset(block: (bufferInputStream: BufferedInputStream) -> Unit) {
    block(this)
    reset()
}

fun String.toJfxColor(): JfxColor {
    val color: Color = toColor()
    return JfxColor(color.red.toDouble(), color.green.toDouble(), color.blue.toDouble(), color.alpha.toDouble())
}

// used by featurea.image.TextureLoader to fix texture loading
fun BufferedImage.createGraphicsFlipped(): Graphics2D {
    val graphics: Graphics2D = createGraphics()
    graphics.transform(AffineTransform().apply {
        concatenate(AffineTransform.getScaleInstance(1.0, -1.0))
        concatenate(AffineTransform.getTranslateInstance(0.0, -height.toDouble()))
    })
    return graphics
}

fun SwingNode(content: JComponent): SwingNode {
    return SwingNode().apply { this.content = content }
}
