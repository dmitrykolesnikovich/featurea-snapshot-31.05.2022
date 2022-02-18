// IMPORTANT package `com.badlogic.gdx.backends.lwjgl` is done intentionally to access `LwjglGL20`
package com.badlogic.gdx.backends.lwjgl

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.PixelFormat
import java.awt.Canvas
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

fun GL20(): GL20 = LwjglGL20()

class LwjglCanvasContext(val action: () -> Unit) : Canvas() {

    private val frame: JFrame = JFrame()

    init {
        frame.pack()
        frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        frame.contentPane.add(this)
    }

    override fun addNotify() {
        super.addNotify()
        SwingUtilities.invokeLater { create() }
    }

    override fun removeNotify() {
        super.removeNotify()
        SwingUtilities.invokeLater { destroy() }
    }

    private fun create() {
        LwjglNativesLoader.load()
        Display.setParent(this)
        Display.create(PixelFormat(24, 8, 16, 0, 0))
        Gdx.gl = GL20()
        action()
        if (SwingUtilities.isEventDispatchThread()) {
            frame.dispose()
        } else {
            frame.isVisible = false
        }
    }

    private fun destroy() {
        Display.destroy()
    }

}
