package featurea.math

class Angle {

    constructor()

    constructor(value: Float) {
        this.value = value.toDouble()
    }

    constructor(value: Double) {
        this.value = value
    }

    var value: Double = 0.0
        set(value) {
            field = value
            cos = cosDeg(value)
            sin = sinDeg(value)
        }
    var cos: Double = 0.0
        private set
    var sin: Double = 0.0
        private set

    fun isEmpty(): Boolean = value == 0.0

    fun isNotEmpty(): Boolean = value != 0.0

    operator fun plusAssign(value: Double) {
        this.value += value
    }

    operator fun minusAssign(value: Double) {
        this.value -= value
    }

    fun assign(value: Double): Angle {
        this.value = value
        return this
    }

    fun assign(value: String): Angle {
        this.value = value.toDouble()
        return this
    }

    fun assign(original: Angle): Angle {
        this.value = original.value
        return this
    }

}
