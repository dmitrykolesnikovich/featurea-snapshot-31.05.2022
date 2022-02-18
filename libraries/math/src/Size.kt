package featurea.math

data class Size(var width: Float = 0f, var height: Float = 0f) {

    constructor(width: Int, height: Int) : this(width.toFloat(), height.toFloat())

    fun isEmpty(): Boolean {
        return width == 0f && height == 0f
    }

    fun isNotEmpty(): Boolean {
        return !isEmpty()
    }

    fun assign(width: Float, height: Float): Size {
        this.width = width
        this.height = height
        return this
    }

    fun assign(width: Int, height: Int): Size {
        return assign(width.toFloat(), height.toFloat())
    }

    fun assign(size: Size): Size {
        return assign(size.width, size.height)
    }

    fun assign(value: String): Size {
        val (width, height) = value.split(",").map { it.trim().toFloat() }
        this.width = width
        this.height = height
        return this
    }

    fun clear() {
        width = 0f
        height = 0f
    }

    override fun equals(other: Any?): Boolean =
        if (other is Size) other.width == width && other.height == height else false

    fun equals(width: Float, height: Float): Boolean {
        return this.width == width && this.height == height
    }

    override fun hashCode(): Int {
        var result = width.hashCode()
        result = 31 * result + height.hashCode()
        return result
    }

    override fun toString(): String = "Size(width=$width, height=$height)"

    inner class Result {

        operator fun component1() = width
        operator fun component2() = height
        private var lock: Any? = null // just for now todo delete this

        fun apply(width: Float, height: Float): Result {
            if (lock != null) {
                println("breakpoint")
            }
            check(lock == null)
            lock = Unit
            this@Size.width = width
            this@Size.height = height
            lock = null
            return this
        }

    }

}

fun String.toSize(): Size = Size().apply { assign(this@toSize) }

fun Size.swap(): Size = apply {
    val width = width
    val height = height
    this.width = height
    this.height = width
}

val Size.aspectRatio: Float get() = width / height
