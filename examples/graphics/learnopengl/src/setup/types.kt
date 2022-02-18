package featurea.examples.learnopengl

const val YAW: Float = -90.0f
const val PITCH: Float = 0.0f
const val SPEED: Float = 0.05f
const val SENSITIVITY: Float = 0.2f
const val ZOOM: Float = 45.0f

class Test(val context: Context) {
    val blendFunction: BlendFunction = BlendFunction()
    val textureFilter: TextureFilter = TextureFilter()
    val sampling: Sampling = Sampling()
    var diffuseTexturePath: String? = null
    var specularTexturePath: String? = null
    var emissionTexturePath: String? = null
    var alphaTest: Float = 0.1f
    var alphaTest2: Float = 0.1f
    var rotationAngle: Double = 0.0
    var rotationIncrement: Float = 0f
    var rotationAxis: Vector = Vector()
    var viewTranslation: Vector = Vector()
    var near: Float = 1f
    var far: Float = 100f
    var fov: Float = 45f
    var radius: Float = 10.0f
}

class BlendFunction {
    var sourceFactor: Int = ONE
    var destinationFactor: Int = ZERO
    var color: Color = transparentColor
}

enum class CameraMovement {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT,
}

class TextureFilter {
    var minFilter: Int = NEAREST_MIPMAP_LINEAR
    var magFilter: Int = LINEAR
}

class Camera {

    var position: Vector = Vector()
    var front: Vector = Vector()
    var up: Vector = Vector()
    var right: Vector = Vector()
    var worldUp: Vector = Vector()
    var yaw: Float = 0f
    var pitch: Float = 0f
    var speed: Float = 0f
    var sensitivity: Float = 0f
    var zoom: Float = 0f
    private val viewMatrix: Matrix = Matrix()

    constructor(pos: Vector = Vector(), up: Vector = Vector(0.0f, 1.0f, 0.0f), yaw: Float = YAW, pitch: Float = PITCH) {
        front = Vector(0.0f, 0.0f, -1.0f)
        speed = SPEED
        sensitivity = SENSITIVITY
        zoom = ZOOM
        position = pos
        worldUp = up
        this.yaw = yaw
        this.pitch = pitch
        updateCameraVectors()
    }

    fun viewMatrix(): Matrix = viewMatrix.assignLookAt(position, position + front, up)

}
