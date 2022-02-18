package featurea.font.content.samples

import featurea.layout.Camera
import featurea.app.Screen
import featurea.app.Layer
import featurea.Colors
import featurea.Colors.yellowgreen
import featurea.res.Bundle
import featurea.res.AssetNotFoundException
import featurea.font.FontContentProvider
import featurea.font.content.Font
import featurea.font.content.GlyphLayout
import featurea.font.content.HorizontalAlign.CENTER
import featurea.font.content.VerticalAlign.WRAP
import featurea.font.content.samples.Content.arial16Font
import featurea.opengl.Graphics
import featurea.runtime.Service
import featurea.runtime.Module
import featurea.runtime.create
import featurea.runtime.import
import featurea.runtime.Constructor
import featurea.graphics.TextureBatch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import featurea.content.loadContent

const val FAB = "/Users/dmitrykolesnikovich/workspace/wheelsinspace/platform/plugins/font-bundle-plugin/samples/font-bundle-plugin-samples/src/main/resources/testFontBundle.fab"

fun launch() = ApplicationContext {
    GlobalScope.launch {
        val content = import<Content>()
        val system = import<System>()
        system.workingDir = FAB
        loadContent(content.resources) {
            screen = Screen {
                layers.add(MyLayer())
            }
        }
    }
}

class Sample3Layer(module: Module) : Layer(module) {

    private val text: String = "abc defg hijkl mnopqr stuvxwz"
    private val capacity: Int = text.length
    private val camera = Camera { size.assign(300f, 300f) }
    private val textureBatch = TextureBatch { coordinates = camera.coordinates }
    private val fontAssetLoader = import<FontContentProvider>()
    private lateinit var glyphLayout: GlyphLayout
    lateinit var font: Font
    private val graphics = import<Graphics>()

    override fun onCreateComponent() {
        textureBatch.capacity = capacity
        font = fontAssetLoader.fontCache[arial16Font] ?: throw AssetNotFoundException(arial16Font)
        glyphLayout = GlyphLayout()
        glyphLayout.font = font
        glyphLayout.horizontalAlign = CENTER
        glyphLayout.verticalAlign = WRAP
        glyphLayout.bounds.assign(30f, 0f, 65f, 300f)
        glyphLayout.update(text)
        textureBatch.initTexture(font.texturePath)
    }

    override suspend fun onUpdateScreen() {
        graphics.clear(yellowgreen)
        graphics.use(camera) {
            graphics.clear(Colors.black)
            if (textureBatch.isDirty) {
                for (line in glyphLayout.lines) {
                    for (glyphVertex in line.glyphVertices) {
                        with(glyphVertex) {
                            textureBatch.drawTexture(x1, y1, x2, y2, u1, v1, u2, v2)
                        }
                    }
                }
            }
            textureBatch.flush()
        }
    }

    override fun onInvalidateLayout() {
        camera.invalidateLayout()
    }

    override fun onInvalidateGraphics() {
        textureBatch.clear()
    }

}

@Constructor
private fun Component.MyLayer(init: Sample3Layer.() -> Unit = {}) = create(init)
