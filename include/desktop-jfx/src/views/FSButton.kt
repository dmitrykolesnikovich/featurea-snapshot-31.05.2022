package featurea.desktop.jfx

import javafx.scene.control.Button

class FSButton : Button() {

    init {
        stylesheets.add("featurea/jfx/button.css".externalPath)
    }

}
