package featurea.extractGif

import featurea.desktop.azFileComparator
import featurea.desktop.copyInputStreamToFile
import featurea.desktop.newInputStream
import featurea.desktop.reset
import featurea.jvm.createNewFileAndDirs
import featurea.jvm.normalizedPath
import featurea.jvm.writeText
import featurea.math.Size
import featurea.jvm.userHomePath
import kotlinx.coroutines.runBlocking
import java.awt.image.BufferedImage
import java.io.*
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.ImageIO.createImageInputStream
import javax.imageio.metadata.IIOMetadataNode
import javax.imageio.stream.ImageInputStream

private const val DEFAULT_GIF_FPS = 12.0

object GifFramesExtractor {

    // IMPORTANT do not replace this with InputStream, it won't work
    fun getFramesDirForGif(gifFileInputStream: BufferedInputStream, outputDir: File) {
        if (outputDir.exists()) return
        try {
            val frameCount: Int
            val loopCount: Int
            // frames
            val pngFrames = ArrayList<InputStream>()
            val gifDecoder = GifDecoder()
            gifDecoder.read(gifFileInputStream.newInputStream())
            val n = gifDecoder.frameCount
            for (index in 0 until n) {
                val image: BufferedImage = gifDecoder.getFrame(index) ?: error("index: $index")
                val frameOutputStream = ByteArrayOutputStream()
                ImageIO.write(image, "png", frameOutputStream)
                val frameInputStream = ByteArrayInputStream(frameOutputStream.toByteArray())
                pngFrames.add(frameInputStream)
            }
            val data = getFramesDirForGif(pngFrames, outputDir.absolutePath)
            Collections.sort(data, azFileComparator)
            frameCount = data.size
            loopCount = gifDecoder.loopCount
            if (frameCount != 0) {
                // manifest
                gifFileInputStream.reset {
                    val size = Size()
                    val imageInputStream = createImageInputStream(it)
                    val fps = getGifFps(imageInputStream, size, frameCount)
                    val file = File(outputDir, "manifest.properties")
                    val properties = mutableMapOf<String, String>()
                    properties["fps"] = fps.joinToString { it.toString() }
                    properties["loopCount"] = loopCount.toString()
                    properties["frameCount"] = fps.size.toString()
                    val userHomePathRegex = "^$userHomePath/".toRegex() // quickfix todo improve
                    properties["frames"] = data.joinToString { it.normalizedPath.replace(userHomePathRegex, "") }
                    properties["size"] = "${size.width},  ${size.height}"
                    properties.writeText(file)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /*internals*/

    private fun getFramesDirForGif(frames: List<InputStream>, outputDir: String): List<File> {
        val result = ArrayList<File>()
        for (i in frames.indices) {
            val pngFrame = frames[i]
            val to = "$outputDir/$i.png"
            val toFile = File(to)
            result.add(toFile)
            toFile.createNewFileAndDirs()
            runBlocking {
                toFile.copyInputStreamToFile(pngFrame)
            }
        }
        return result
    }

    @Throws(IOException::class)
    private fun getGifFps(imageInputStream: ImageInputStream, size: Size, frameCount: Int): List<Double> {
        val fps = ArrayList<Double>()
        val reader = ImageIO.getImageReadersBySuffix("gif").next()
        reader.input = imageInputStream
        for (imageIndex in 0 until frameCount) {
            val imageMetaData = reader.getImageMetadata(imageIndex)
            val metaFormatName = imageMetaData.nativeMetadataFormatName
            val root = imageMetaData.getAsTree(metaFormatName) as IIOMetadataNode
            val imageDescriptorNode = newMetadataNode(root, "ImageDescriptor")
            size.width = java.lang.Float.parseFloat(imageDescriptorNode.getAttribute("imageWidth"))
            size.height = java.lang.Float.parseFloat(imageDescriptorNode.getAttribute("imageHeight"))
            val graphicsControlExtensionNode = newMetadataNode(root, "GraphicControlExtension")
            val delayTime = graphicsControlExtensionNode.getAttribute("delayTime")
            try {
                val delayTimeDouble = java.lang.Double.parseDouble(delayTime)
                if (delayTimeDouble != 0.0) {
                    fps.add(100 / delayTimeDouble)
                } else {
                    fps.add(DEFAULT_GIF_FPS)
                }
            } catch (e: NumberFormatException) {
                fps.add(DEFAULT_GIF_FPS)
            }

        }
        return fps
    }

    private fun newMetadataNode(rootNode: IIOMetadataNode, nodeName: String): IIOMetadataNode {
        val nNodes = rootNode.length
        for (i in 0 until nNodes) {
            if (rootNode.item(i).nodeName.compareTo(nodeName, ignoreCase = true) == 0) {
                return rootNode.item(i) as IIOMetadataNode
            }
        }
        val node = IIOMetadataNode(nodeName)
        rootNode.appendChild(node)
        return node
    }

}
