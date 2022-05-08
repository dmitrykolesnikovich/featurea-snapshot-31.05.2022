package featurea.examples.learnopengl

import featurea.app.ApplicationComponent
import featurea.app.ApplicationDelegate
import featurea.content.Content
import featurea.graphics.Graphics
import featurea.image.ImageContent
import featurea.input.Input
import featurea.input.InputEvent
import featurea.input.InputListener
import featurea.keyboard.KeyEvent
import featurea.keyboard.KeyListener
import featurea.keyboard.Keyboard
import featurea.loader.Loader
import featurea.opengl.Opengl
import featurea.opengl.OpenglProxy
import featurea.runtime.import
import featurea.shader.ShaderContent
import featurea.text.TextContent
import featurea.window.Window

class Context : ApplicationComponent(), ApplicationDelegate, InputListener, KeyListener {

    val content: Content = import()
    val keyboard: Keyboard = import()
    val input: Input = import()
    val graphics: Graphics = import()
    val gl: Opengl = import(OpenglProxy)
    val loader: Loader = import()
    val shaderContent: ShaderContent = import()
    val test: Test = Test(this)
    val textContent: TextContent = import()
    val imageContent: ImageContent = import()
    val window: Window = import()

    var isStudioRuntime: Boolean = false
    private lateinit var initBlock: Test.() -> Unit
    private lateinit var loadBlock: suspend (progress: Float) -> Unit
    private lateinit var updateBlock: (elapsedTime: Float) -> Unit
    private lateinit var inputBlock: (event: InputEvent) -> Unit
    private lateinit var keyBlock: (event: KeyEvent) -> Unit
    private lateinit var resizeBlock: () -> Unit

    init {
        content.providedResources.addAll(featurea.examples.learnopengl.bootstrapResources)
    }

    init {
        input.addListener(this)
        keyboard.addListener(this)
    }

    fun init(block: Test.() -> Unit) {
        this.initBlock = block
    }

    fun update(block: (elapsedTime: Float) -> Unit) {
        this.updateBlock = block
    }

    fun load(block: suspend (progress: Float) -> Unit) {
        this.loadBlock = block
    }

    fun input(block: (event: InputEvent) -> Unit) {
        inputBlock = block
    }

    fun keyboard(block: (event: KeyEvent) -> Unit) {
        keyBlock = block
    }

    fun resize(block: () -> Unit) {
        resizeBlock = block
    }

    override suspend fun load(progress: Float) {
        loadBlock(progress)
    }

    override fun update(elapsedTime: Float) {
        updateBlock(elapsedTime)
    }

    override fun onInput(inputEvent: InputEvent) {
        inputBlock(inputEvent)
    }

    override fun onKey(keyEvent: KeyEvent) {
        keyBlock(keyEvent)
    }

    override fun resize(width: Int, height: Int) {
        resizeBlock()
    }

}
