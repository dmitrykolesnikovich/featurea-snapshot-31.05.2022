package featurea.window

import com.jogamp.opengl.GL
import com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT

internal fun GL.clearWithMainPanelColorQuickfix() {
    val (red, green, blue, alpha) = MainPanelColor
    glClearColor(red, green, blue, alpha)
    glClear(GL_COLOR_BUFFER_BIT)
}
