package featurea.opengl

import com.jogamp.opengl.awt.GLJPanel
import featurea.desktop.MainPanelProxy
import featurea.runtime.Action
import featurea.runtime.Provide
import featurea.runtime.import
import featurea.runtime.provide
import featurea.window.Window
import featurea.window.WindowInitListener

@Provide(OpenglProxy::class)
val provideOpenglProxy: Action = {
    val gl: OpenglImpl = OpenglImpl(module)
    provide(OpenglProxy(gl))
    val window: Window = import()
    window.listeners.add(WindowInitListener {
        val mainPanel: GLJPanel = import(MainPanelProxy)
        gl.context = mainPanel.gl.gL2
    })
}
