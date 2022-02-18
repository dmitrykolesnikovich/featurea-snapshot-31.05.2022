package featurea.fullscreen

import featurea.layout.View

class FullScreenButton : View() {
    var isFullScreen: Boolean = false
    var onChangeFullScreen: (() -> Unit)? = null
    var enterFullScreenImage: String? = null
}
