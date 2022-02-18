package featurea.layout

class CameraStyle(val camera: Camera) {

    var horizontal: Horizontal? = null
    var vertical: Vertical? = null

    fun assign(original: CameraStyle) {
        this.horizontal = original.horizontal
        this.vertical = original.vertical
    }

}
