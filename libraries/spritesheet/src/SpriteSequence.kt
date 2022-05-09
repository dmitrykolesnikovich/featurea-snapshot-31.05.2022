package featurea.spritesheet

import featurea.content.Resource
import featurea.utils.findIndexBySum
import featurea.utils.replaceWith
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import featurea.utils.splitAndTrim

class SpriteSequence(override val module: Module) : Component {

    private val spriteCache: SpriteCache = import()

    private lateinit var resource: Resource
    private val frameDurations = mutableListOf<Float>()
    var maxTime: Float = 0f
        private set
    var maxFrameIndex: Int = 0
        private set
    var maxLoopCount: Int = 0
        private set
    val isNotLoop: Boolean get() = maxLoopCount == 1
    val isInfinite: Boolean get() = maxLoopCount == 0
    private var currentTime: Float = 0f
    private var currentFrameIndex: Int = 0
    private var currentLoopCount: Int = 0
    private var isRunning: Boolean = false
    var currentSpritePath: String? = null
        private set
    var currentSprite: Sprite? = null
        private set
    val hasSprite: Boolean get() = currentSprite != null

    fun init(resource: Resource) {
        this.resource = resource

        // 1. setup
        frameDurations.replaceWith(resource.manifest.fps.splitAndTrim(",").map { 1000f / it.toFloat() })
        maxTime = frameDurations.sum()
        maxLoopCount = resource.manifest.loopCount
        maxFrameIndex = resource.manifest.frameCount - 1

        // 2. validate
        check(maxTime > 0)
        check(maxLoopCount >= 0)
        check(maxFrameIndex >= 0)
    }

    fun start() {
        currentTime = 0f
        currentFrameIndex = 0
        currentLoopCount = 0
        resume()
    }

    fun resume() {
        if (isRunning) return
        isRunning =
            isInfinite || currentLoopCount < maxLoopCount || currentFrameIndex < maxFrameIndex || currentTime < maxTime
    }

    fun pause() {
        isRunning = false
    }

    fun stop() {
        pause()
        currentTime = maxTime
        currentFrameIndex = maxFrameIndex
        currentLoopCount = maxLoopCount
    }

    fun reset() {
        currentTime = 0f
        currentFrameIndex = 0
        currentLoopCount = 0
        isRunning = false
        currentSprite = null
    }

    fun updateSprite(frameTime: Float, invalidate: () -> Unit = {}) {
        // 1. currentTime, currentFrameIndex, currentLoopCount
        if (isRunning) {
            currentTime += frameTime
            if (currentTime >= maxTime) {
                currentLoopCount++
            }
            currentTime %= maxTime
            currentFrameIndex = frameDurations.findIndexBySum(currentTime)
        }

        // 2. isRunning
        if (!isInfinite) {
            if (currentLoopCount >= maxLoopCount) {
                stop()
            }
        }

        // 3. currentTexture, currentTextureRegion
        val spritePath: String = resource.manifest.frames[currentFrameIndex]
        val sprite: Sprite? = spriteCache.sprites[spritePath]
        if (currentSpritePath != spritePath || currentSprite != sprite) {
            invalidate()
        }
        currentSpritePath = spritePath
        currentSprite = sprite
    }

}

// constructor
fun Component.SpriteSequence() = SpriteSequence(module)
