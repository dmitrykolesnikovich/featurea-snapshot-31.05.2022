package featurea.studio.editor

import featurea.desktop.MainNodeProxy
import featurea.desktop.MainPanelProxy
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.FSSplitPane
import featurea.desktop.jfx.resizableWithParent
import featurea.desktop.runOnJfxThread
import featurea.runtime.Module
import featurea.studio.editor.components.RmlTableView
import featurea.studio.editor.components.RmlTreeView
import featurea.studio.project.Project
import featurea.window.Window
import javafx.geometry.Orientation.HORIZONTAL
import javafx.geometry.Orientation.VERTICAL
import javafx.scene.Node
import javafx.scene.control.Tab
import javafx.scene.layout.StackPane
import javafx.stage.Stage

fun EditorModule() = Module {
    onInit { editorModule ->
        await(MainNodeProxy::class)

        runOnJfxThread {
            editorModule.importComponent<Editor>()
        }
    }
    onCreate { editorModule ->
        val mainNode: Node = editorModule.importComponent(MainNodeProxy)
        val mainStage: Stage = editorModule.importComponent(MainStageProxy)
        val project: Project = editorModule.importComponent()
        val rmlTableView: RmlTableView = editorModule.importComponent()
        val rmlTreeView: RmlTreeView = editorModule.importComponent()

        project.addEditorModuleTab(editorModule, Tab().apply {
            content = FSSplitPane().apply {
                orientation = HORIZONTAL
                val structurePanel = FSSplitPane().apply {
                    orientation = VERTICAL
                    items.add(rmlTreeView)
                    items.add(rmlTableView.apply {
                        resizableWithParent = false
                    })
                }
                items.add(structurePanel.apply {
                    resizableWithParent = false
                })
                items.add(StackPane().apply {
                    children.add(mainNode)
                })
                setDividerPositions(300.0 / mainStage.width)
            }
        })
    }

}
