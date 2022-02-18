package featurea.studio.editor.features

import featurea.Application
import featurea.desktop.jfx.onChange
import featurea.rml.buildApplication
import featurea.runOnUpdateOnJfxThread
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.EditorFeature
import featurea.window.Window
import featurea.studio.editor.components.*
import featurea.utils.watchBlocking

class ScreenEditorFeature(editorModule: Module) : EditorFeature(editorModule) {

    private val app: Application = import()
    private val window: Window = import()
    private val selectionService: SelectionService = import()

    init {
        editor.editingScope = EditingScope.Application(module)
        editor.selectionService.selections.onChange {
            editor.updateEditorUi {
                window.invalidate()
            }
        }

        editor.tab.rmlResourceProperty.watchBlocking {
            app.delegate = editorModule.buildApplication(editor.tab.rmlResource)
        }
        editor.selectionService.selectedResourceAttributeProperty.watch {
            app.runOnUpdateOnJfxThread {
                window.invalidate()
            }
        }
        editor.tab.isDirtyProperty.watch {
            app.runOnUpdateOnJfxThread {
                window.invalidate()
            }
        }
        selectionService.selectedResourceAttributeProperty.watch {
            window.invalidate()
        }
    }

}
