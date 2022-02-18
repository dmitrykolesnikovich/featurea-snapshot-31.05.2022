package featurea.desktop

import com.jogamp.opengl.awt.GLJPanel
import featurea.desktop.jfx.registerGlobalKeyEvents
import featurea.runtime.*
import javafx.scene.Node
import javafx.stage.Stage
import javax.swing.JPanel

/*dependencies*/

val artifact = Artifact("featurea.desktop") {
    "DesktopApplication" to DesktopApplication::class
    "MainNodeProxy" to MainNodeProxy::class
    "MainPanelProxy" to MainPanelProxy::class
    "MainStageProxy" to MainStageProxy::class
}

fun DependencyBuilder.MainNodePlugin(plugin: Plugin<MainNodeProxy>) = install(plugin)
fun DependencyBuilder.MainPanelPlugin(plugin: Plugin<MainPanelProxy>) = install(plugin)

class MainNodeProxy(override val delegate: Node) : Proxy<Node> {
    companion object : Delegate<Node>(MainNodeProxy::class)
}

class MainPanelProxy(override val delegate: GLJPanel) : Proxy<GLJPanel> {
    companion object : Delegate<GLJPanel>(MainPanelProxy::class)
}

class MainStageProxy(override val delegate: Stage) : Proxy<Stage> {
    companion object : Delegate<Stage>(MainStageProxy::class)

    init {
        delegate.registerGlobalKeyEvents()
    }
}
