package featurea.examples.learnopengl

fun blendFuncOf(value: String): Int = when (value) {
    "ZERO" -> ZERO
    "ONE" -> ONE
    "SRC_COLOR" -> SRC_COLOR
    "ONE_MINUS_SRC_COLOR" -> ONE_MINUS_SRC_COLOR
    "DST_COLOR" -> DST_COLOR
    "ONE_MINUS_DST_COLOR" -> ONE_MINUS_DST_COLOR
    "SRC_ALPHA" -> SRC_ALPHA
    "ONE_MINUS_SRC_ALPHA" -> ONE_MINUS_SRC_ALPHA
    "DST_ALPHA" -> DST_ALPHA
    "ONE_MINUS_DST_ALPHA" -> ONE_MINUS_DST_ALPHA
    "CONSTANT_COLOR" -> CONSTANT_COLOR
    "ONE_MINUS_CONSTANT_COLOR" -> ONE_MINUS_CONSTANT_COLOR
    "CONSTANT_ALPHA" -> CONSTANT_ALPHA
    "ONE_MINUS_CONSTANT_ALPHA" -> ONE_MINUS_CONSTANT_ALPHA
    "SRC_ALPHA_SATURATE" -> SRC_ALPHA_SATURATE
    else -> error("value: $value")
}

fun Matrix(init: Matrix.() -> Unit): Matrix {
    MATRIX.assignIdentity()
    MATRIX.init()
    return MATRIX
}

private val MATRIX: Matrix = Matrix()

fun Program.bindTexture(name: String, slot: Int, texturePath: String?, sampling: Sampling) {
    val imageContent: ImageContent = module.import()
    val texture: Texture? = imageContent.findTextureOrNull(texturePath)
    bindTexture(name, slot, texture, sampling)
}

fun textureMinFilterOf(value: String): Int = when (value) {
    "NEAREST" -> NEAREST
    "LINEAR" -> LINEAR
    "NEAREST_MIPMAP_NEAREST" -> NEAREST_MIPMAP_NEAREST
    "NEAREST_MIPMAP_LINEAR" -> NEAREST_MIPMAP_LINEAR
    "LINEAR_MIPMAP_NEAREST" -> LINEAR_MIPMAP_NEAREST
    "LINEAR_MIPMAP_LINEAR" -> LINEAR_MIPMAP_LINEAR
    else -> error("value: $value")
}

fun textureMagFilterOf(value: String): Int = when (value) {
    "NEAREST" -> NEAREST
    "LINEAR" -> LINEAR
    else -> error("value: $value")
}

fun Camera.processInput(xoffset: Float, yoffset: Float, constrainPitch: Boolean = true) {
    var xoffset: Float = xoffset
    var yoffset: Float = yoffset
    xoffset *= sensitivity
    yoffset *= sensitivity

    yaw += xoffset
    pitch += yoffset

    // make sure that when pitch is out of bounds, screen doesn't get flipped
    if (constrainPitch) {
        if (pitch > 89.0f)
            pitch = 89.0f
        if (pitch < -89.0f)
            pitch = -89.0f
    }

    // update Front, Right and Up Vectors using the updated Euler angles
    updateCameraVectors()
}

fun Camera.processKeyboard(direction: CameraMovement, elapsedTime: Float) {
    val velocity: Float = speed * elapsedTime
    when (direction) {
        CameraMovement.FORWARD -> position += front * velocity
        CameraMovement.BACKWARD -> position -= front * velocity
        CameraMovement.LEFT -> position -= right * velocity
        CameraMovement.RIGHT -> position += right * velocity
    }
}

fun Camera.processScroll(yoffset: Float) {
    zoom -= yoffset
    if (zoom < 1.0f) zoom = 1.0f
    if (zoom > 45.0f) zoom = 45.0f
}

/*internals*/

private fun Camera.updateCameraVectors() {
    // calculate the new Front vector
    val front: Vector /* = featurea.math.Vector3 */ = Vector()
    front.x = cos(yaw.toRadians()) * cos(pitch.toRadians())
    front.y = sin(pitch.toRadians())
    front.z = sin(yaw.toRadians()) * cos(pitch.toRadians())
    this.front = front.normalize()

    // also re-calculate the Right and Up vector
    right.assignCross(this.front, worldUp).normalize()
    up.assignCross(right, this.front).normalize()
}
