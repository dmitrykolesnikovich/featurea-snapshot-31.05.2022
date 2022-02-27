package featurea.studio.project

import featurea.Application
import featurea.System
import featurea.content.mainDocument
import featurea.content.mainProject
import featurea.desktop.MainNodeProxy
import featurea.desktop.MainStageProxy
import featurea.desktop.jfx.FSProgressBarDialog
import featurea.desktop.runOnJfxThread
import featurea.jvm.normalizedPath
import featurea.loader.Loader
import featurea.rml.readRmlResource
import featurea.rml.reader.RmlContent
import featurea.runtime.import
import featurea.runtime.proxyScope
import featurea.studio.editor.Editor
import featurea.studio.editor.EditorDelegate
import featurea.studio.editor.EditorRuntime
import featurea.text.TextContent
import featurea.window.Window
import javafx.embed.swing.SwingNode
import javafx.scene.Node
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

inline fun <reified T : EditorDelegate> Project.openEditor(documentId: String) {
    val mainStage: Stage = import(MainStageProxy)
    val project: Project = import()
    val textContent: TextContent = import()

    val progressDialog: FSProgressBarDialog = FSProgressBarDialog("Open $documentId", mainStage)
    progressDialog.show()
    thread {
        proxyScope {
            /*await(MainPanelProxy::class)*/
            onInitModule { editorModule ->
                editorModule.importComponent<Window>()
            }
            EditorRuntime(projectModule = module) { editorModule ->
                val app: Application = editorModule.importComponent()
                val editor: Editor = editorModule.importComponent()
                val loader: Loader = editorModule.importComponent()
                val mainNode: Node = editorModule.importComponent(MainNodeProxy)
                val rmlContent: RmlContent = editorModule.importComponent()
                val system: System = editorModule.importComponent()
                val window: Window = editorModule.importComponent()

                editor.delegate = editorModule.importComponent<T>()
                runBlocking {
                    loader.loadRmlResource(project.rmlResource) {
                        window.useCamera = false
                        app.isEnable = false
                        val filePath: String = project.rmlFile.normalizedPath
                        system.properties.mainProject = filePath
                        system.properties.mainDocument = documentId
                        val resourcePath: String = "$filePath:/$documentId"
                        // rmlContent.remove(filePath) // quickfix todo uncomment
                        textContent.removeCachedText(filePath)
                        editor.tab.rmlResource = rmlContent.readRmlResource(resourcePath)
                        editor.tab.resolveIcon()
                        runOnJfxThread {
                            progressDialog.close()
                            mainNode as SwingNode
                            mainNode.content.requestFocus()
                            mainNode.content.requestFocusInWindow()
                        }
                    }
                }
            }
        }
    }
}
