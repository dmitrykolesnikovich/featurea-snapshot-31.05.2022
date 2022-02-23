package featurea.math

data class Rectangle(var x1: Float = 0f, var y1: Float = 0f, var x2: Float = 0f, var y2: Float = 0f) {

    constructor(rectangle: Rectangle) : this(rectangle.x1, rectangle.y1, rectangle.x2, rectangle.y2)

    val left: Float get() = x1
    val top: Float get() = y1
    val right: Float get() = x2
    val bottom: Float get() = y2
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (x1 + x2) / 2f
    val centerY: Float get() = (y1 + y2) / 2f
    fun isEmpty(): Boolean = width == 0f && height == 0f
    fun isNotEmpty(): Boolean = !isEmpty()
    private val sizeResult: Size = Size()
    val size: Size get() = sizeResult.assign(width, height)

    fun assign(x: Float, y: Float): Rectangle {
        this.x1 = x
        this.y1 = y
        this.x2 = x
        this.y2 = y
        return this
    }

    fun assign(x1: Float, y1: Float, x2: Float, y2: Float): Rectangle {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
        return this
    }

    fun assign(size: Size): Rectangle {
        x1 = 0f
        y1 = 0f
        x2 = size.width
        y2 = size.height
        return this
    }

    fun assign(point: Point, size: Size): Rectangle {
        this.x1 = point.x
        this.y1 = point.y
        this.x2 = this.x1 + size.width
        this.y2 = this.y1 + size.height
        return this
    }

    fun assign(point: Point, rectangle: Rectangle): Rectangle {
        this.x1 = rectangle.x1 + point.x
        this.y1 = rectangle.y1 + point.y
        this.x2 = rectangle.x2 + point.x
        this.y2 = rectangle.y2 + point.y
        return this
    }

    infix fun assign(rectangle: Rectangle): Rectangle {
        this.x1 = rectangle.x1
        this.y1 = rectangle.y1
        this.x2 = rectangle.x2
        this.y2 = rectangle.y2
        return this
    }

    infix fun assign(rectangle: Rectangle.Result): Rectangle {
        this.x1 = rectangle.left
        this.y1 = rectangle.top
        this.x2 = rectangle.right
        this.y2 = rectangle.bottom
        return this
    }

    fun assign(value: String): Rectangle {
        val (x1, y1, x2, y2) = value.split(",").map { it.trim().toFloat() }
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
        return this
    }

    fun move(vector: Vector2): Rectangle {
        return move(vector.x, vector.y)
    }

    fun move(dx: Float, dy: Float): Rectangle {
        x1 += dx
        y1 += dy
        x2 += dx
        y2 += dy
        return this
    }

    fun contains(x: Float, y: Float): Boolean = left < x && x < right && top < y && y < bottom

    fun ensureSize(size: Size): Rectangle {
        return ensureSize(size.width, size.height)
    }

    fun ensureSize(width: Float, height: Float): Rectangle {
        x2 = x1 + width
        y2 = y1 + height
        return this
    }

    fun increaseSize(width: Int, height: Int): Rectangle {
        x2 += width
        y2 += height
        return this
    }

    fun clear(): Rectangle {
        x1 = 0f
        y1 = 0f
        x2 = 0f
        y2 = 0f
        return this
    }

    fun intersects(rectangle: Rectangle): Boolean {
        return rectangle.x1 < x2 && rectangle.x2 > x1 && rectangle.y1 < y2 && rectangle.y2 > y1
    }

    fun normalizedRectangle(): Rectangle = Rectangle(
        this
    ).apply {
        if (x2 < x1) {
            val temp = x1; x1 = x2; x2 = temp
        }
        if (y2 < y1) {
            val temp = y1; y1 = y2; y2 = temp
        }
    }

    fun isValid(): Boolean = isRectangleValid(x1, y1, x2, y2)

    override fun equals(other: Any?): Boolean =
        (other is Rectangle && other.x1 == x1 && other.y1 == y1 && other.x2 == x2 && other.y2 == y2) ||
                (other is Rectangle.Result && other.left == x1 && other.top == y1 && other.right == x2 && other.bottom == y2)

    override fun toString(): String = "Rectangle($x1, $y1, $x2, $y2)"

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + x1.toBits()
        result = prime * result + y1.toBits()
        result = prime * result + x2.toBits()
        result = prime * result + y2.toBits()
        return result
    }

    inner class Result {

        operator fun component1() = x1
        operator fun component2() = y1
        operator fun component3() = x2
        operator fun component4() = y2
        val width: Float get() = x2 - x1
        val height: Float get() = y2 - y1
        val left: Float get() = x1
        val top: Float get() = y1
        val right: Float get() = x2
        val bottom: Float get() = y2

        fun assign(x1: Float, y1: Float, x2: Float, y2: Float): Result {
            this@Rectangle.x1 = x1
            this@Rectangle.y1 = y1
            this@Rectangle.x2 = x2
            this@Rectangle.y2 = y2
            return this
        }

        fun assign(rectangle: Rectangle): Result {
            this@Rectangle.x1 = rectangle.x1
            this@Rectangle.y1 = rectangle.y1
            this@Rectangle.x2 = rectangle.x2
            this@Rectangle.y2 = rectangle.y2
            return this
        }

        fun contains(x: Float, y: Float): Boolean = this@Rectangle.contains(x, y)

        override fun toString(): String = "$left, $top, $right, $bottom"
    }

}

fun Rectangle.assignInscribe(outerSize: Size, innerSize: Size) {
    var x1 = 0f
    var y1 = 0f
    var newWidth = innerSize.width
    var newHeight = innerSize.height
    if (outerSize.isNotEmpty()) {
        val widthRatio = innerSize.width / outerSize.width
        val heightRatio = innerSize.height / outerSize.height
        if (widthRatio >= heightRatio) {
            newWidth = outerSize.width
            newHeight = innerSize.height / widthRatio
            y1 = (outerSize.height - newHeight) / 2
        } else {
            newHeight = outerSize.height
            newWidth = innerSize.width / heightRatio
            x1 = (outerSize.width - newWidth) / 2
        }
    }
    assign(x1, y1, x1 + newWidth, y1 + newHeight)
}

fun Rectangle.withPadding(padding: Rectangle): Rectangle {
    return Rectangle(x1 + padding.x1, y1 + padding.y1, x2 - padding.x2, y2 - padding.y2)
}

fun Rectangle.assignPadding(left: Float, top: Float, right: Float, bottom: Float) {
    x1 += left
    y1 += top
    x2 -= right
    y2 -= bottom
}

fun isRectangleValid(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
    return x2 > x1 && y2 > y1
}

fun Rectangle.swap() {
    val width = width
    val height = height
    x2 = x1 + height
    y2 = y1 + width
}

data class IntRectangle(val x1: Int, val y1: Int, val x2: Int, val y2: Int) // quickfix todo replace with better concept
