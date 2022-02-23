package featurea.math

typealias Coordinates = () -> Transform

class Transform(val matrix: Matrix = Matrix()) {

    var ox: Float = 0f
        private set
    var oy: Float = 0f
        private set
    var tx: Float = 0f
        private set
    var ty: Float = 0f
        private set
    var sx: Float = 1f
        private set
    var sy: Float = 1f
        private set

    private val editor: Editor = Editor()
    private val transform: Transform = this

    constructor(original: Transform) : this() {
        edit {
            assign(original)
        }
    }

    fun edit(update: Editor.() -> Unit): Transform {
        editor.update()
        updateMatrix()
        return this
    }

    inner class Editor {

        fun assign(original: Transform) {
            ox = original.ox
            oy = original.oy
            tx = original.tx
            ty = original.ty
            sx = original.sx
            sy = original.sy
            matrix.assign(original.matrix)
        }

        fun assignOrigin(ox: Float, oy: Float) {
            transform.ox = ox
            transform.oy = oy
        }

        fun assignOrigin(origin: Point) {
            transform.ox = origin.x * sx
            transform.oy = origin.y * sy
        }

        fun assignTranslation(vector: Vector2) {
            assignTranslation(vector.x, vector.y)
        }

        fun assignTranslation(tx: Int, ty: Int) {
            assignTranslation(tx.toFloat(), ty.toFloat())
        }

        fun assignTranslation(tx: Float, ty: Float) {
            transform.tx = tx
            transform.ty = ty
        }

        fun translate(vector: Vector2) {
            translate(vector.x, vector.y)
        }

        fun translate(dx: Int, dy: Int) {
            translate(dx.toFloat(), dy.toFloat())
        }

        fun translate(dx: Float, dy: Float) {
            tx += dx * sx
            ty += dy * sy
        }

        fun assignScale(sx: Float, sy: Float) {
            transform.sx = sx
            transform.sy = sy
        }

        fun assignScale(scale: Float) {
            transform.sx = scale
            transform.sy = scale
        }

        fun scale(ox: Float, oy: Float, scalar: Float) {
            scaleResult.scale(tx, ty, ox, oy, scalar)
            tx = scaleResult.x
            ty = scaleResult.y
            sx *= scalar
            sy *= scalar
        }

        /*internals*/

        private val scaleResult: Vector2 = Vector2()

    }

    override fun toString() = "Transform(origin=($ox, $oy), translation=($tx, $ty), scale=($sx, $sy))"

    /*internals*/

    private fun updateMatrix() {
        matrix.assignIdentity()
        matrix.translate(tx - ox, ty - oy)
        matrix.scale(sx, sy)
    }

}
