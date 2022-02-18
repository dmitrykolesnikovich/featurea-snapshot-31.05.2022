package featurea.studio.editor.features

import featurea.content.ResourceTag
import featurea.loader.Loader
import featurea.runtime.Module
import featurea.runtime.import
import featurea.studio.editor.EditorFeature
import featurea.window.Window

class AutoloadFontEditorFeature(module: Module) : EditorFeature(module) {

    private val loader: Loader = import()
    private val window: Window = import()

    override fun filterAttribute(rmlTag: ResourceTag, key: String, value: String): String {
        if (key == "font") {
            loader.loadResource(value) {
                window.invalidate()
            }
        }
        return value
    }

}
