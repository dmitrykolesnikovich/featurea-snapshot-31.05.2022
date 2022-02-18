package featurea.math

import kotlin.math.tan

typealias Matrix = Matrix4

private const val i00: Int = 0
private const val i01: Int = 1
private const val i02: Int = 2
private const val i03: Int = 3
private const val i10: Int = 4
private const val i11: Int = 5
private const val i12: Int = 6
private const val i13: Int = 7
private const val i20: Int = 8
private const val i21: Int = 9
private const val i22: Int = 10
private const val i23: Int = 11
private const val i30: Int = 12
private const val i31: Int = 13
private const val i32: Int = 14
private const val i33: Int = 15

class Matrix4 {

    var m00: Float = 0f
        private set
    var m01: Float = 0f
        private set
    var m02: Float = 0f
        private set
    var m03: Float = 0f
        private set
    var m10: Float = 0f
        private set
    var m11: Float = 0f
        private set
    var m12: Float = 0f
        private set
    var m13: Float = 0f
        private set
    var m20: Float = 0f
        private set
    var m21: Float = 0f
        private set
    var m22: Float = 0f
        private set
    var m23: Float = 0f
        private set
    var m30: Float = 0f
        private set
    var m31: Float = 0f
        private set
    var m32: Float = 0f
        private set
    var m33: Float = 0f
        private set

    init {
        assignIdentity()
    }

    fun assignIdentity(): Matrix4 {
        return assign(IdentityMatrixArray16)
    }

    fun assignZero(): Matrix4 {
        return assign(ZeroMatrixArray16)
    }

    fun assign(original: Matrix4): Matrix4 {
        m00 = original.m00; m01 = original.m01; m02 = original.m02; m03 = original.m03
        m10 = original.m10; m11 = original.m11; m12 = original.m12; m13 = original.m13
        m20 = original.m20; m21 = original.m21; m22 = original.m22; m23 = original.m23
        m30 = original.m30; m31 = original.m31; m32 = original.m32; m33 = original.m33
        return this
    }

    fun assign(original: FloatArray): Matrix4 {
        m00 = original[i00]; m01 = original[i01]; m02 = original[i02]; m03 = original[i03]
        m10 = original[i10]; m11 = original[i11]; m12 = original[i12]; m13 = original[i13]
        m20 = original[i20]; m21 = original[i21]; m22 = original[i22]; m23 = original[i23]
        m30 = original[i30]; m31 = original[i31]; m32 = original[i32]; m33 = original[i33]
        return this
    }

    fun assignTranslation(translation: Vector2): Matrix4 {
        return assignTranslation(translation.x, translation.y)
    }

    fun assignTranslation(translation: Vector3): Matrix4 {
        return assignTranslation(translation.x, translation.y, translation.z)
    }

    fun assignTranslation(tx: Float, ty: Float): Matrix4 {
        assignIdentity()
        return translate(tx, ty)
    }

    fun assignTranslation(tx: Float, ty: Float, tz: Float): Matrix4 {
        assignIdentity()
        return translate(tx, ty, tz)
    }

    fun translate(translation: Vector2): Matrix4 {
        return translate(translation.x, translation.y)
    }

    fun translate(dx: Float, dy: Float): Matrix4 {
        m30 += m00 * dx + m10 * dy
        m31 += m01 * dx + m11 * dy
        m32 += m02 * dx + m12 * dy
        m33 += m03 * dx + m13 * dy
        return this
    }

    fun translate(translation: Vector3): Matrix4 {
        return translate(translation.x, translation.y, translation.z)
    }

    fun translate(dx: Float, dy: Float, dz: Float): Matrix4 {
        m30 += m00 * dx + m10 * dy + m20 * dz
        m31 += m01 * dx + m11 * dy + m21 * dz
        m32 += m02 * dx + m12 * dy + m22 * dz
        m33 += m03 * dx + m13 * dy + m23 * dz
        return this
    }

    fun assignScale(scale: Vector2): Matrix4 {
        return assignScale(scale.x, scale.y)
    }

    fun assignScale(sx: Float, sy: Float): Matrix4 {
        assignIdentity()
        return scale(sx, sy)
    }

    fun scale(sfactor: Float): Matrix4 {
        return scale(sfactor, sfactor, sfactor)
    }

    fun scale(sx: Float, sy: Float): Matrix4 {
        m00 *= sx; m01 *= sx; m02 *= sx; m03 *= sx
        m10 *= sy; m11 *= sy; m12 *= sy; m13 *= sy
        return this
    }

    fun scale(sx: Float, sy: Float, sz: Float): Matrix4 {
        m00 *= sx; m01 *= sx; m02 *= sx; m03 *= sx
        m10 *= sy; m11 *= sy; m12 *= sy; m13 *= sy
        m20 *= sz; m21 *= sz; m22 *= sz; m23 *= sz
        return this
    }

    fun assignMultiplication(right: Matrix4): Matrix4 {
        return assignMultiplication(this, right)
    }

    fun assignMultiplication(left: Matrix4, right: Matrix4): Matrix4 {
        val m00 = left.m00 * right.m00 + left.m10 * right.m01 + left.m20 * right.m02 + left.m30 * right.m03
        val m01 = left.m01 * right.m00 + left.m11 * right.m01 + left.m21 * right.m02 + left.m31 * right.m03
        val m02 = left.m02 * right.m00 + left.m12 * right.m01 + left.m22 * right.m02 + left.m32 * right.m03
        val m03 = left.m03 * right.m00 + left.m13 * right.m01 + left.m23 * right.m02 + left.m33 * right.m03
        val m10 = left.m00 * right.m10 + left.m10 * right.m11 + left.m20 * right.m12 + left.m30 * right.m13
        val m11 = left.m01 * right.m10 + left.m11 * right.m11 + left.m21 * right.m12 + left.m31 * right.m13
        val m12 = left.m02 * right.m10 + left.m12 * right.m11 + left.m22 * right.m12 + left.m32 * right.m13
        val m13 = left.m03 * right.m10 + left.m13 * right.m11 + left.m23 * right.m12 + left.m33 * right.m13
        val m20 = left.m00 * right.m20 + left.m10 * right.m21 + left.m20 * right.m22 + left.m30 * right.m23
        val m21 = left.m01 * right.m20 + left.m11 * right.m21 + left.m21 * right.m22 + left.m31 * right.m23
        val m22 = left.m02 * right.m20 + left.m12 * right.m21 + left.m22 * right.m22 + left.m32 * right.m23
        val m23 = left.m03 * right.m20 + left.m13 * right.m21 + left.m23 * right.m22 + left.m33 * right.m23
        val m30 = left.m00 * right.m30 + left.m10 * right.m31 + left.m20 * right.m32 + left.m30 * right.m33
        val m31 = left.m01 * right.m30 + left.m11 * right.m31 + left.m21 * right.m32 + left.m31 * right.m33
        val m32 = left.m02 * right.m30 + left.m12 * right.m31 + left.m22 * right.m32 + left.m32 * right.m33
        val m33 = left.m03 * right.m30 + left.m13 * right.m31 + left.m23 * right.m32 + left.m33 * right.m33
        this.m00 = m00; this.m01 = m01; this.m02 = m02; this.m03 = m03
        this.m10 = m10; this.m11 = m11; this.m12 = m12; this.m13 = m13
        this.m20 = m20; this.m21 = m21; this.m22 = m22; this.m23 = m23
        this.m30 = m30; this.m31 = m31; this.m32 = m32; this.m33 = m33
        return this
    }

    fun assignOrtho(x: Int, y: Int, width: Int, height: Int): Matrix4 {
        return assignOrtho(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
    }

    fun assignOrtho(x: Float, y: Float, width: Float, height: Float): Matrix4 {
        return assignOrtho(left = x, right = x + width, bottom = y + height, top = y, near = 1f/*0?*/, far = 0f/*1?*/)
    }

    // https://www.khronos.org/registry/OpenGL-Refpages/gl2.1/xhtml/glOrtho.xml
    fun assignOrtho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): Matrix4 {
        val ox = 2 / (right - left)
        val oy = 2 / (top - bottom)
        val oz = -2 / (far - near)
        val tx = -(right + left) / (right - left)
        val ty = -(top + bottom) / (top - bottom)
        val tz = -(far + near) / (far - near)
        m00 = ox; m01 = 0f; m02 = 0f; m03 = 0f
        m10 = 0f; m11 = oy; m12 = 0f; m13 = 0f
        m20 = 0f; m21 = 0f; m22 = oz; m23 = 0f
        m30 = tx; m31 = ty; m32 = tz; m33 = 1f
        return this
    }

    // https://github.com/libgdx/libgdx/blob/c981d375463126f18f13a62e3aadd98da1b8352d/gdx/src/com/badlogic/gdx/math/Matrix4.java#L478
    fun assignPerspective(near: Float, far: Float, fovy: Float, aspectRatio: Float): Matrix4 {
        val ty: Float = (1f / tan(fovy * (PI / 180f) / 2f))
        val tx: Float = ty / aspectRatio
        val tz: Float = (far + near) / (near - far)
        val t2: Float = 2f * far * near / (near - far)
        m00 = tx; m01 = 0f; m02 = 0f; m03 = 0f
        m10 = 0f; m11 = ty; m12 = 0f; m13 = 0f
        m20 = 0f; m21 = 0f; m22 = tz; m23 = -1f
        m30 = 0f; m31 = 0f; m32 = t2; m33 = 0f
        return this
    }

    fun assignLookAt(position: Vector3, target: Vector3, up: Vector3): Matrix4 {
        val right: Matrix = Pool.matrix0
        val direction: Vector = Pool.vertex0
        direction.assign(target).minusAssign(position)
        assignLookAt(direction, up)
        assignMultiplication(right.assignTranslation(-position.x, -position.y, -position.z))
        return this
    }

    fun assignLookAt(direction: Vector3, up: Vector3): Matrix4 {
        val vectorX: Vector = Pool.vertex1
        val vectorY: Vector = Pool.vertex2
        val vectorZ: Vector = Pool.vertex3
        vectorZ.assign(direction).normalize()
        vectorX.assign(direction).assignCross(up).normalize()
        vectorY.assign(vectorX).assignCross(vectorZ).normalize()
        assignIdentity()
        m00 = vectorX.x
        m01 = vectorY.x
        m02 = -vectorZ.x
        m10 = vectorX.y
        m11 = vectorY.y
        m12 = -vectorZ.y
        m20 = vectorX.z
        m21 = vectorY.z
        m22 = -vectorZ.z
        return this
    }

    // https://math.stackexchange.com/a/2093322/262743
    fun rotate(angle: Angle): Matrix4 {
        val cos = angle.cos.toFloat()
        val sin = angle.sin.toFloat()
        assignMultiplication(ROTATION_MATRIX.apply {
            m00 = cos; m01 = -sin
            m10 = sin; m11 = cos
        })
        return this
    }

    fun rotate(x: Float, y: Float, angle: Double): Matrix4 {
        return rotate(x, y, ANGLE.assign(angle))
    }

    fun rotate(x: Float, y: Float, angle: Angle): Matrix4 {
        translate(x, y)
        rotate(angle)
        translate(-x, -y)
        return this
    }

    fun rotate(axis: Vector, angle: Double): Matrix4 {
        val (x, y, z) = axis
        return rotate(x, y, z, angle)
    }

    fun rotate(axis: Vector, angle: Angle): Matrix4 {
        val (x, y, z) = axis
        return rotate(x, y, z, angle)
    }

    fun rotate(x: Float, y: Float, z: Float, angle: Double): Matrix4 {
        return rotate(x, y, z, ANGLE.assign(angle))
    }

    fun rotate(x: Float, y: Float, z: Float, angle: Angle): Matrix4 {
        translate(x, y, z)
        rotate(angle)
        translate(-x, -y, -z)
        return this
    }

    override fun toString() = """
        $m00, $m10, $m20, $m30
        $m01, $m11, $m21, $m31
        $m02, $m12, $m22, $m32
        $m03, $m13, $m23, $m33
        """.trimIndent()

    private object Pool {
        val vertex0: Vector get() = VERTICES[0].assign(0f, 0f, 0f)
        val vertex1: Vector get() = VERTICES[1].assign(0f, 0f, 0f)
        val vertex2: Vector get() = VERTICES[2].assign(0f, 0f, 0f)
        val vertex3: Vector get() = VERTICES[3].assign(0f, 0f, 0f)
        private val VERTICES: Array<Vector> = Array(size = 4) { Vector() }
        val matrix0: Matrix get() = MATRICES[0].assignIdentity()
        private val MATRICES: Array<Matrix> = Array(size = 1) { Matrix() }
    }

}

fun Matrix.copyToArray16(array16: FloatArray): FloatArray = array16.apply {
    this[i00] = m00; this[i01] = m01; this[i02] = m02; this[i03] = m03
    this[i10] = m10; this[i11] = m11; this[i12] = m12; this[i13] = m13
    this[i20] = m20; this[i21] = m21; this[i22] = m22; this[i23] = m23
    this[i30] = m30; this[i31] = m31; this[i32] = m32; this[i33] = m33
}

/*
M times v equals v`: M * v = v`

|m00 m10 m20 m30|    x    x`
|m01 m11 m21 m31|    y    y`
|m02 m12 m22 m32|    0    0
|m03 m13 m23 m33|    1    1
*/
operator fun Matrix4.times(vector: Vector2): Vector2 {
    val matrix = this
    val x = matrix.m00 * vector.x + matrix.m10 * vector.y + matrix.m30
    val y = matrix.m01 * vector.x + matrix.m11 * vector.y + matrix.m31
    return Point(x, y)
}

operator fun Matrix4.times(vector: Vector2.Result): Vector2 {
    val matrix = this
    val (vx, vy) = vector
    val x = matrix.m00 * vx + matrix.m10 * vy + matrix.m30
    val y = matrix.m01 * vx + matrix.m11 * vy + matrix.m31
    return Point(x, y)
}

/*
        M * v = v`

|m00 m10 m20 m30|    x    x`
|m01 m11 m21 m31|    y    y`
|m02 m12 m22 m32|    z    z`
|m03 m13 m23 m33|    1    1
*/
operator fun Matrix4.times(vector: Vector3): Vector3 {
    val matrix = this
    val x = matrix.m00 * vector.x + matrix.m10 * vector.y + matrix.m20 * vector.z + matrix.m30
    val y = matrix.m01 * vector.x + matrix.m11 * vector.y + matrix.m21 * vector.z + matrix.m31
    val z = matrix.m02 * vector.x + matrix.m12 * vector.y + matrix.m22 * vector.z + matrix.m32
    return Vector(x, y, z)
}

/*internals*/

private val IdentityMatrixArray16: FloatArray = floatArrayOf(
    1.0f, 0.0f, 0.0f, 0.0f,
    0.0f, 1.0f, 0.0f, 0.0f,
    0.0f, 0.0f, 1.0f, 0.0f,
    0.0f, 0.0f, 0.0f, 1.0f
)

private val ZeroMatrixArray16: FloatArray = floatArrayOf(
    0.0f, 0.0f, 0.0f, 0.0f,
    0.0f, 0.0f, 0.0f, 0.0f,
    0.0f, 0.0f, 0.0f, 0.0f,
    0.0f, 0.0f, 0.0f, 0.0f
)

private val ROTATION_MATRIX: Matrix = Matrix() // quickfix todo improve

private val ANGLE: Angle = Angle()
