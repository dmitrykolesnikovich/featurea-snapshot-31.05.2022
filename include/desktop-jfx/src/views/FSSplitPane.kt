package featurea.desktop.jfx

import javafx.scene.control.SplitPane

open class FSSplitPane : SplitPane() {

    init {
        stylesheets.add("featurea/jfx/splitpane-style.css".externalPath)
    }

}
